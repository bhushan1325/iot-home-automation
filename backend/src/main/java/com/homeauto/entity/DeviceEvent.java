package com.homeauto.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "device_events", indexes = {
    @Index(name = "idx_event_device_time", columnList = "device_id, created_at"),
    @Index(name = "idx_event_created_at", columnList = "created_at")
})
public class DeviceEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_state", length = 10)
    private DeviceState oldState;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_state", length = 10)
    private DeviceState newState;

    @Column(length = 500)
    private String details;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public DeviceEvent() {}

    public DeviceEvent(Device device, EventType eventType, DeviceState oldState, DeviceState newState, String details) {
        this.device = device;
        this.eventType = eventType;
        this.oldState = oldState;
        this.newState = newState;
        this.details = details;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
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
