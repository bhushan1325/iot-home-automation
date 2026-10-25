package com.homeauto.dto;

import com.homeauto.entity.DeviceState;
import jakarta.validation.constraints.NotNull;

public class CommandRequest {

    @NotNull(message = "Requested state is required (ON or OFF)")
    private DeviceState requestedState;

    public CommandRequest() {}

    public CommandRequest(DeviceState requestedState) {
        this.requestedState = requestedState;
    }

    public DeviceState getRequestedState() {
        return requestedState;
    }

    public void setRequestedState(DeviceState requestedState) {
        this.requestedState = requestedState;
    }
}
