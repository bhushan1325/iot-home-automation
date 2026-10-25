package com.homeauto.dto;

import com.homeauto.entity.DeviceState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class Esp32AckRequest {

    @NotBlank(message = "Command UUID is required")
    private String commandUuid;

    @NotBlank(message = "Device key is required")
    private String deviceKey;

    @NotNull(message = "Executed state is required")
    private DeviceState executedState;

    private boolean success = true;
    private String errorMessage;

    public Esp32AckRequest() {}

    public Esp32AckRequest(String commandUuid, String deviceKey, DeviceState executedState, boolean success) {
        this.commandUuid = commandUuid;
        this.deviceKey = deviceKey;
        this.executedState = executedState;
        this.success = success;
    }

    public String getCommandUuid() {
        return commandUuid;
    }

    public void setCommandUuid(String commandUuid) {
        this.commandUuid = commandUuid;
    }

    public String getDeviceKey() {
        return deviceKey;
    }

    public void setDeviceKey(String deviceKey) {
        this.deviceKey = deviceKey;
    }

    public DeviceState getExecutedState() {
        return executedState;
    }

    public void setExecutedState(DeviceState executedState) {
        this.executedState = executedState;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
