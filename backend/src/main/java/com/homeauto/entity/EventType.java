package com.homeauto.entity;

public enum EventType {
    STATE_CHANGED,
    HEARTBEAT,
    COMMAND_CREATED,
    COMMAND_DISPATCHED,
    COMMAND_ACKNOWLEDGED,
    OFFLINE_DETECTED,
    ONLINE_DETECTED,
    ERROR
}
