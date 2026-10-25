package com.homeauto.dto;

import com.homeauto.entity.Device;
import com.homeauto.entity.DeviceState;
import com.homeauto.entity.DeviceType;

import java.time.LocalDateTime;

public class DeviceDto {
    private Long id;
    private String deviceKey;
    private String name;
    private Long roomId;
    private String roomName;
    private DeviceType type;
    private Integer pinNumber;
    private Integer relayChannel;
    private boolean enabled;
    private boolean isOnline;
    private LocalDateTime lastSeenAt;
    private DeviceState confirmedState;
    private DeviceState lastRequestedState;

    public DeviceDto() {}

    public static DeviceDto fromEntity(Device device) {
        DeviceDto dto = new DeviceDto();
        dto.setId(device.getId());
        dto.setDeviceKey(device.getDeviceKey());
        dto.setName(device.getName());
        if (device.getRoom() != null) {
            dto.setRoomId(device.getRoom().getId());
            dto.setRoomName(device.getRoom().getName());
        }
        dto.setType(device.getType());
        dto.setPinNumber(device.getPinNumber());
        dto.setRelayChannel(device.getRelayChannel());
        dto.setEnabled(device.isEnabled());
        dto.setOnline(device.isOnline());
        dto.setLastSeenAt(device.getLastSeenAt());
        dto.setConfirmedState(device.getConfirmedState());
        dto.setLastRequestedState(device.getLastRequestedState());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceKey() {
        return deviceKey;
    }

    public void setDeviceKey(String deviceKey) {
        this.deviceKey = deviceKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public DeviceType getType() {
        return type;
    }

    public void setType(DeviceType type) {
        this.type = type;
    }

    public Integer getPinNumber() {
        return pinNumber;
    }

    public void setPinNumber(Integer pinNumber) {
        this.pinNumber = pinNumber;
    }

    public Integer getRelayChannel() {
        return relayChannel;
    }

    public void setRelayChannel(Integer relayChannel) {
        this.relayChannel = relayChannel;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public LocalDateTime getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(LocalDateTime lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public DeviceState getConfirmedState() {
        return confirmedState;
    }

    public void setConfirmedState(DeviceState confirmedState) {
        this.confirmedState = confirmedState;
    }

    public DeviceState getLastRequestedState() {
        return lastRequestedState;
    }

    public void setLastRequestedState(DeviceState lastRequestedState) {
        this.lastRequestedState = lastRequestedState;
    }
}
