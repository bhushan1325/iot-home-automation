package com.homeauto.dto;

import com.homeauto.entity.DeviceType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DeviceUpdateRequest {

    @NotBlank(message = "Device name is required")
    private String name;

    @NotNull(message = "Room ID is required")
    private Long roomId;

    @NotNull(message = "Device type is required")
    private DeviceType type;

    @NotNull(message = "Safe GPIO pin number is required")
    private Integer pinNumber;

    @NotNull(message = "Relay channel is required (1-8)")
    @Min(1)
    @Max(8)
    private Integer relayChannel;

    private Boolean enabled;

    public DeviceUpdateRequest() {}

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

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
