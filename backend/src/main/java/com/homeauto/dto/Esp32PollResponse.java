package com.homeauto.dto;

public class Esp32PollResponse {
    private boolean hasPendingCommand;
    private Long commandId;
    private String commandUuid;
    private String deviceKey;
    private Integer pinNumber;
    private Integer relayChannel;
    private String requestedState;
    private long serverTime = System.currentTimeMillis();

    public Esp32PollResponse() {
        this.hasPendingCommand = false;
    }

    public static Esp32PollResponse empty() {
        Esp32PollResponse res = new Esp32PollResponse();
        res.setHasPendingCommand(false);
        return res;
    }

    public boolean isHasPendingCommand() {
        return hasPendingCommand;
    }

    public void setHasPendingCommand(boolean hasPendingCommand) {
        this.hasPendingCommand = hasPendingCommand;
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
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

    public String getRequestedState() {
        return requestedState;
    }

    public void setRequestedState(String requestedState) {
        this.requestedState = requestedState;
    }

    public long getServerTime() {
        return serverTime;
    }

    public void setServerTime(long serverTime) {
        this.serverTime = serverTime;
    }
}
