package com.homeauto.dto;

import com.homeauto.entity.DeviceState;
import jakarta.validation.constraints.NotBlank;

public class Esp32HeartbeatRequest {

    @NotBlank(message = "Device key is required")
    private String deviceKey;

    private DeviceState confirmedState;
    private Long uptimeSeconds;
    private Integer wifiRssi;
    private String ipAddress;
    private String firmwareVersion = "1.0.0";

    public Esp32HeartbeatRequest() {}

    public Esp32HeartbeatRequest(String deviceKey, DeviceState confirmedState, Long uptimeSeconds, Integer wifiRssi, String ipAddress) {
        this.deviceKey = deviceKey;
        this.confirmedState = confirmedState;
        this.uptimeSeconds = uptimeSeconds;
        this.wifiRssi = wifiRssi;
        this.ipAddress = ipAddress;
    }

    public String getDeviceKey() {
        return deviceKey;
    }

    public void setDeviceKey(String deviceKey) {
        this.deviceKey = deviceKey;
    }

    public DeviceState getConfirmedState() {
        return confirmedState;
    }

    public void setConfirmedState(DeviceState confirmedState) {
        this.confirmedState = confirmedState;
    }

    public Long getUptimeSeconds() {
        return uptimeSeconds;
    }

    public void setUptimeSeconds(Long uptimeSeconds) {
        this.uptimeSeconds = uptimeSeconds;
    }

    public Integer getWifiRssi() {
        return wifiRssi;
    }

    public void setWifiRssi(Integer wifiRssi) {
        this.wifiRssi = wifiRssi;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }
}
