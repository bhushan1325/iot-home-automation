package com.homeauto.dto;

import com.homeauto.entity.Command;
import com.homeauto.entity.CommandStatus;
import com.homeauto.entity.DeviceState;

import java.time.LocalDateTime;

public class CommandResponseDto {
    private Long id;
    private String commandUuid;
    private Long deviceId;
    private String deviceKey;
    private String deviceName;
    private DeviceState requestedState;
    private CommandStatus status;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private LocalDateTime acknowledgedAt;

    public CommandResponseDto() {}

    public static CommandResponseDto fromEntity(Command command) {
        CommandResponseDto dto = new CommandResponseDto();
        dto.setId(command.getId());
        dto.setCommandUuid(command.getCommandUuid());
        if (command.getDevice() != null) {
            dto.setDeviceId(command.getDevice().getId());
            dto.setDeviceKey(command.getDevice().getDeviceKey());
            dto.setDeviceName(command.getDevice().getName());
        }
        dto.setRequestedState(command.getRequestedState());
        dto.setStatus(command.getStatus());
        dto.setErrorMessage(command.getErrorMessage());
        dto.setCreatedAt(command.getCreatedAt());
        dto.setSentAt(command.getSentAt());
        dto.setAcknowledgedAt(command.getAcknowledgedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCommandUuid() {
        return commandUuid;
    }

    public void setCommandUuid(String commandUuid) {
        this.commandUuid = commandUuid;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceKey() {
        return deviceKey;
    }

    public void setDeviceKey(String deviceKey) {
        this.deviceKey = deviceKey;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public DeviceState getRequestedState() {
        return requestedState;
    }

    public void setRequestedState(DeviceState requestedState) {
        this.requestedState = requestedState;
    }

    public CommandStatus getStatus() {
        return status;
    }

    public void setStatus(CommandStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public LocalDateTime getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public void setAcknowledgedAt(LocalDateTime acknowledgedAt) {
        this.acknowledgedAt = acknowledgedAt;
    }
}
