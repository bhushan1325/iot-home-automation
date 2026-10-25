package com.homeauto.dto;

import java.time.LocalDateTime;
import java.util.List;

public class DashboardSummaryDto {
    private long totalDevices;
    private long onlineDevices;
    private long activeDevices; // ON state
    private long offlineDevices;
    private LocalDateTime serverTime = LocalDateTime.now();
    private List<DeviceDto> devices;

    public DashboardSummaryDto() {}

    public DashboardSummaryDto(long totalDevices, long onlineDevices, long activeDevices, long offlineDevices, List<DeviceDto> devices) {
        this.totalDevices = totalDevices;
        this.onlineDevices = onlineDevices;
        this.activeDevices = activeDevices;
        this.offlineDevices = offlineDevices;
        this.devices = devices;
        this.serverTime = LocalDateTime.now();
    }

    public long getTotalDevices() {
        return totalDevices;
    }

    public void setTotalDevices(long totalDevices) {
        this.totalDevices = totalDevices;
    }

    public long getOnlineDevices() {
        return onlineDevices;
    }

    public void setOnlineDevices(long onlineDevices) {
        this.onlineDevices = onlineDevices;
    }

    public long getActiveDevices() {
        return activeDevices;
    }

    public void setActiveDevices(long activeDevices) {
        this.activeDevices = activeDevices;
    }

    public long getOfflineDevices() {
        return offlineDevices;
    }

    public void setOfflineDevices(long offlineDevices) {
        this.offlineDevices = offlineDevices;
    }

    public LocalDateTime getServerTime() {
        return serverTime;
    }

    public void setServerTime(LocalDateTime serverTime) {
        this.serverTime = serverTime;
    }

    public List<DeviceDto> getDevices() {
        return devices;
    }

    public void setDevices(List<DeviceDto> devices) {
        this.devices = devices;
    }
}
