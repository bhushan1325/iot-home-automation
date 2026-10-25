package com.homeauto.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "devices", indexes = {
    @Index(name = "idx_device_key", columnList = "device_key", unique = true),
    @Index(name = "idx_device_room", columnList = "room_id")
})
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_key", nullable = false, unique = true, length = 64)
    private String deviceKey;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeviceType type = DeviceType.LIGHT;

    @Column(name = "pin_number", nullable = false)
    private Integer pinNumber;

    @Column(name = "relay_channel", nullable = false)
    private Integer relayChannel;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "is_online", nullable = false)
    private boolean isOnline = false;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "confirmed_state", nullable = false, length = 10)
    private DeviceState confirmedState = DeviceState.OFF;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_requested_state", nullable = false, length = 10)
    private DeviceState lastRequestedState = DeviceState.OFF;

    @Column(name = "device_secret", nullable = false, length = 128)
    private String deviceSecret;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Device() {}

    public Device(String deviceKey, String name, Room room, DeviceType type, Integer pinNumber,
                  Integer relayChannel, String deviceSecret) {
        this.deviceKey = deviceKey;
        this.name = name;
        this.room = room;
        this.type = type;
        this.pinNumber = pinNumber;
        this.relayChannel = relayChannel;
        this.deviceSecret = deviceSecret;
        this.enabled = true;
        this.isOnline = false;
        this.confirmedState = DeviceState.OFF;
        this.lastRequestedState = DeviceState.OFF;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
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

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
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

    public String getDeviceSecret() {
        return deviceSecret;
    }

    public void setDeviceSecret(String deviceSecret) {
        this.deviceSecret = deviceSecret;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
