package com.homeauto.service;

import com.homeauto.entity.*;
import com.homeauto.repository.CommandRepository;
import com.homeauto.repository.DeviceEventRepository;
import com.homeauto.repository.DeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@EnableScheduling
public class DeviceWatchdogService {

    private static final Logger log = LoggerFactory.getLogger(DeviceWatchdogService.class);

    private final DeviceRepository deviceRepository;
    private final CommandRepository commandRepository;
    private final DeviceEventRepository deviceEventRepository;

    @Value("${app.esp32.offline-threshold-seconds:35}")
    private long offlineThresholdSeconds;

    @Value("${app.esp32.command-timeout-seconds:20}")
    private long commandTimeoutSeconds;

    public DeviceWatchdogService(DeviceRepository deviceRepository,
                                 CommandRepository commandRepository,
                                 DeviceEventRepository deviceEventRepository) {
        this.deviceRepository = deviceRepository;
        this.commandRepository = commandRepository;
        this.deviceEventRepository = deviceEventRepository;
    }

    @Scheduled(fixedRate = 10000)
    @Transactional
    public void monitorDeviceLiveness() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(offlineThresholdSeconds);
        List<Device> offlineCandidates = deviceRepository.findByIsOnlineTrueAndLastSeenAtBefore(threshold);

        for (Device device : offlineCandidates) {
            device.setOnline(false);
            deviceRepository.save(device);

            deviceEventRepository.save(new DeviceEvent(
                    device,
                    EventType.OFFLINE_DETECTED,
                    device.getConfirmedState(),
                    device.getConfirmedState(),
                    "Device missed heartbeats for over " + offlineThresholdSeconds + "s. Marked OFFLINE."
            ));

            log.warn("Device {} has timed out (last seen: {}). Marked OFFLINE.",
                    device.getDeviceKey(), device.getLastSeenAt());
        }
    }

    @Scheduled(fixedRate = 10000)
    @Transactional
    public void expireTimedOutCommands() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(commandTimeoutSeconds);

        List<Command> timedOutSentCommands = commandRepository.findByStatusAndCreatedAtBefore(CommandStatus.SENT, threshold);
        for (Command cmd : timedOutSentCommands) {
            cmd.setStatus(CommandStatus.TIMED_OUT);
            cmd.setErrorMessage("Hardware command timed out. Device failed to acknowledge within " + commandTimeoutSeconds + "s.");
            commandRepository.save(cmd);

            deviceEventRepository.save(new DeviceEvent(
                    cmd.getDevice(),
                    EventType.ERROR,
                    cmd.getDevice().getConfirmedState(),
                    cmd.getDevice().getConfirmedState(),
                    "Command UUID " + cmd.getCommandUuid() + " timed out without acknowledgement."
            ));

            log.warn("Command {} timed out awaiting acknowledgement.", cmd.getCommandUuid());
        }
    }
}
