package com.homeauto.service;

import com.homeauto.dto.Esp32AckRequest;
import com.homeauto.dto.Esp32HeartbeatRequest;
import com.homeauto.dto.Esp32PollResponse;
import com.homeauto.entity.*;
import com.homeauto.repository.CommandRepository;
import com.homeauto.repository.DeviceEventRepository;
import com.homeauto.repository.DeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class Esp32Service {

    private static final Logger log = LoggerFactory.getLogger(Esp32Service.class);

    private final DeviceRepository deviceRepository;
    private final CommandRepository commandRepository;
    private final DeviceEventRepository deviceEventRepository;

    public Esp32Service(DeviceRepository deviceRepository,
                        CommandRepository commandRepository,
                        DeviceEventRepository deviceEventRepository) {
        this.deviceRepository = deviceRepository;
        this.commandRepository = commandRepository;
        this.deviceEventRepository = deviceEventRepository;
    }

    public Device authenticateDevice(String deviceKey, String deviceToken) {
        if (deviceKey == null || deviceToken == null) {
            throw new AccessDeniedException("Missing ESP32 authentication headers: X-Device-Key or X-Device-Token");
        }

        Device device = deviceRepository.findByDeviceKey(deviceKey)
                .orElseThrow(() -> new AccessDeniedException("Unknown device key: " + deviceKey));

        if (!device.getDeviceSecret().equals(deviceToken)) {
            log.warn("Unauthorized ESP32 token attempt for device key: {}", deviceKey);
            throw new AccessDeniedException("Invalid device token for device key: " + deviceKey);
        }

        return device;
    }

    @Transactional
    public void processHeartbeat(Esp32HeartbeatRequest request, String deviceToken) {
        Device device = authenticateDevice(request.getDeviceKey(), deviceToken);

        boolean wasOffline = !device.isOnline();
        device.setLastSeenAt(LocalDateTime.now());
        device.setOnline(true);

        if (wasOffline) {
            deviceEventRepository.save(new DeviceEvent(
                    device,
                    EventType.ONLINE_DETECTED,
                    device.getConfirmedState(),
                    device.getConfirmedState(),
                    "ESP32 reconnected. IP: " + request.getIpAddress() + ", RSSI: " + request.getWifiRssi() + " dBm"
            ));
            log.info("Device {} came ONLINE (IP: {}, RSSI: {} dBm)",
                    device.getDeviceKey(), request.getIpAddress(), request.getWifiRssi());
        }

        if (request.getConfirmedState() != null && request.getConfirmedState() != device.getConfirmedState()) {
            DeviceState old = device.getConfirmedState();
            device.setConfirmedState(request.getConfirmedState());
            deviceEventRepository.save(new DeviceEvent(
                    device,
                    EventType.STATE_CHANGED,
                    old,
                    request.getConfirmedState(),
                    "Physical state synchronized via heartbeat"
            ));
        }

        deviceRepository.save(device);
    }

    @Transactional
    public Esp32PollResponse pollPendingCommand(String deviceKey, String deviceToken) {
        Device device = authenticateDevice(deviceKey, deviceToken);

        device.setLastSeenAt(LocalDateTime.now());
        device.setOnline(true);
        deviceRepository.save(device);

        List<Command> pendingCommands = commandRepository.findByDeviceIdAndStatusOrderByCreatedAtAsc(
                device.getId(), CommandStatus.PENDING
        );

        if (pendingCommands.isEmpty()) {
            return Esp32PollResponse.empty();
        }

        Command cmd = pendingCommands.get(0);
        cmd.setStatus(CommandStatus.SENT);
        cmd.setSentAt(LocalDateTime.now());
        commandRepository.save(cmd);

        deviceEventRepository.save(new DeviceEvent(
                device,
                EventType.COMMAND_DISPATCHED,
                device.getConfirmedState(),
                cmd.getRequestedState(),
                "Command dispatched to ESP32 (UUID: " + cmd.getCommandUuid() + ")"
        ));

        Esp32PollResponse response = new Esp32PollResponse();
        response.setHasPendingCommand(true);
        response.setCommandId(cmd.getId());
        response.setCommandUuid(cmd.getCommandUuid());
        response.setDeviceKey(device.getDeviceKey());
        response.setPinNumber(device.getPinNumber());
        response.setRelayChannel(device.getRelayChannel());
        response.setRequestedState(cmd.getRequestedState().name());
        response.setServerTime(System.currentTimeMillis());

        log.info("Dispatched pending command {} -> {} to ESP32", cmd.getCommandUuid(), cmd.getRequestedState());
        return response;
    }

    @Transactional
    public void acknowledgeCommand(Esp32AckRequest request, String deviceToken) {
        Device device = authenticateDevice(request.getDeviceKey(), deviceToken);

        Command command = commandRepository.findByCommandUuid(request.getCommandUuid())
                .orElseThrow(() -> new IllegalArgumentException("Command not found with UUID: " + request.getCommandUuid()));

        if (!command.getDevice().getId().equals(device.getId())) {
            throw new AccessDeniedException("Command UUID " + request.getCommandUuid() + " does not belong to device " + device.getDeviceKey());
        }

        command.setAcknowledgedAt(LocalDateTime.now());

        if (request.isSuccess()) {
            command.setStatus(CommandStatus.ACKNOWLEDGED);
            DeviceState oldState = device.getConfirmedState();
            device.setConfirmedState(request.getExecutedState());
            device.setLastSeenAt(LocalDateTime.now());
            device.setOnline(true);
            deviceRepository.save(device);

            deviceEventRepository.save(new DeviceEvent(
                    device,
                    EventType.COMMAND_ACKNOWLEDGED,
                    oldState,
                    request.getExecutedState(),
                    "ESP32 successfully executed command (UUID: " + command.getCommandUuid() + ")"
            ));

            if (oldState != request.getExecutedState()) {
                deviceEventRepository.save(new DeviceEvent(
                        device,
                        EventType.STATE_CHANGED,
                        oldState,
                        request.getExecutedState(),
                        "Confirmed state updated to " + request.getExecutedState()
                ));
            }

            log.info("Command {} ACKNOWLEDGED successfully by ESP32: new state = {}",
                    command.getCommandUuid(), request.getExecutedState());
        } else {
            command.setStatus(CommandStatus.FAILED);
            command.setErrorMessage(request.getErrorMessage() != null ? request.getErrorMessage() : "Execution failed on hardware");
            log.error("Command {} FAILED on hardware: {}", command.getCommandUuid(), command.getErrorMessage());

            deviceEventRepository.save(new DeviceEvent(
                    device,
                    EventType.ERROR,
                    device.getConfirmedState(),
                    device.getConfirmedState(),
                    "Execution error on ESP32: " + command.getErrorMessage()
            ));
        }

        commandRepository.save(command);
    }
}
