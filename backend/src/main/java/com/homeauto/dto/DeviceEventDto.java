package com.homeauto.dto;

import com.homeauto.entity.DeviceEvent;
import com.homeauto.entity.DeviceState;
import com.homeauto.entity.EventType;

import java.time.LocalDateTime;

public class DeviceEventDto {
    private Long id;
    private Long deviceId;
    private String deviceKey;
    private String deviceName;
    private String roomName;
    private EventType eventType;
    private DeviceState oldState;
    private DeviceState newState;
    private String details;
    private LocalDateTime createdAt;

    public DeviceEventDto() {}

    public static DeviceEventDto fromEntity(DeviceEvent event) {
        DeviceEventDto dto = new DeviceEventDto();
        dto.setId(event.getId());
        if (event.getDevice() != null) {
            dto.setDeviceId(event.getDevice().getId());
            dto.setDeviceKey(event.getDevice().getDeviceKey());
            dto.setDeviceName(event.getDevice().getName());
            if (event.getDevice().getRoom() != null) {
                dto.setRoomName(event.getDevice().getRoom().getName());
            }
        }
        dto.setEventType(event.getEventType());
        dto.setOldState(event.getOldState());
        dto.setNewState(event.getNewState());
        dto.setDetails(event.getDetails());
        dto.setCreatedAt(event.getCreatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public DeviceState getOldState() {
        return oldState;
    }

    public void setOldState(DeviceState oldState) {
        this.oldState = oldState;
    }

    public DeviceState getNewState() {
        return newState;
    }

    public void setNewState(DeviceState newState) {
        this.newState = newState;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
