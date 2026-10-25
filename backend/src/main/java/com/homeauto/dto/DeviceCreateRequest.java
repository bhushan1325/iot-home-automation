package com.homeauto.dto;

import com.homeauto.entity.DeviceType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class DeviceCreateRequest {

    @NotBlank(message = "Device key is required")
    @Pattern(regexp = "^[A-Za-z0-9_-]{3,50}$", message = "Device key must be alphanumeric and 3-50 chars")
    private String deviceKey;

    @NotBlank(message = "Device name is required")
    private String name;

    @NotNull(message = "Room ID is required")
    private Long roomId;

    @NotNull(message = "Device type is required")
    private DeviceType type;

    @NotNull(message = "Safe GPIO pin number is required")
    private Integer pinNumber;

    @NotNull(message = "Relay channel is required (1-4)")
    @Min(1)
    @Max(8)
    private Integer relayChannel;

    private String deviceSecret;

    public DeviceCreateRequest() {}

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

    public String getDeviceSecret() {
        return deviceSecret;
    }

    public void setDeviceSecret(String deviceSecret) {
        this.deviceSecret = deviceSecret;
    }
}
