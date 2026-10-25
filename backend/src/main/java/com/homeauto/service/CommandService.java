package com.homeauto.service;

import com.homeauto.dto.CommandResponseDto;
import com.homeauto.entity.*;
import com.homeauto.repository.CommandRepository;
import com.homeauto.repository.DeviceEventRepository;
import com.homeauto.repository.DeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommandService {

    private static final Logger log = LoggerFactory.getLogger(CommandService.class);

    private final CommandRepository commandRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceEventRepository deviceEventRepository;

    public CommandService(CommandRepository commandRepository,
                          DeviceRepository deviceRepository,
                          DeviceEventRepository deviceEventRepository) {
        this.commandRepository = commandRepository;
        this.deviceRepository = deviceRepository;
        this.deviceEventRepository = deviceEventRepository;
    }

    @Transactional
    public CommandResponseDto submitCommand(Long deviceId, DeviceState requestedState) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device not found with id: " + deviceId));

        if (!device.isEnabled()) {
            throw new IllegalStateException("Device '" + device.getName() + "' is disabled and cannot accept commands.");
        }

        // Cancel previous pending commands for the same device to prevent queue build-up
        List<Command> existingPending = commandRepository.findByDeviceIdAndStatusOrderByCreatedAtAsc(deviceId, CommandStatus.PENDING);
        for (Command oldCmd : existingPending) {
            oldCmd.setStatus(CommandStatus.FAILED);
            oldCmd.setErrorMessage("Superseded by new command: " + requestedState);
            commandRepository.save(oldCmd);
        }

        Command command = new Command(device, requestedState);
        Command savedCommand = commandRepository.save(command);

        device.setLastRequestedState(requestedState);
        deviceRepository.save(device);

        deviceEventRepository.save(new DeviceEvent(
                device,
                EventType.COMMAND_CREATED,
                device.getConfirmedState(),
                requestedState,
                "User requested " + requestedState + " (Command UUID: " + savedCommand.getCommandUuid() + ")"
        ));

        log.info("Submitted command UUID {} for device {} -> {}",
                savedCommand.getCommandUuid(), device.getDeviceKey(), requestedState);

        return CommandResponseDto.fromEntity(savedCommand);
    }

    @Transactional
    public List<CommandResponseDto> submitAllOff() {
        List<Device> devices = deviceRepository.findByEnabledTrue();
        List<CommandResponseDto> responses = new ArrayList<>();

        for (Device device : devices) {
            CommandResponseDto res = submitCommand(device.getId(), DeviceState.OFF);
            responses.add(res);
        }

        log.info("Triggered All-Off for {} devices", devices.size());
        return responses;
    }

    public List<CommandResponseDto> getDeviceCommands(Long deviceId) {
        return commandRepository.findByDeviceIdOrderByCreatedAtDesc(deviceId)
                .stream()
                .map(CommandResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<CommandResponseDto> getRecentCommands() {
        return commandRepository.findTop50ByOrderByCreatedAtDesc()
                .stream()
                .map(CommandResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
}
