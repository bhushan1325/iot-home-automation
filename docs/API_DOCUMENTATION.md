# REST API Reference Documentation
## IoT-Based Home Automation System using ESP32

Base Server URL: `http://localhost:8080` (or `http://<SERVER_LAN_IP>:8080`)

All API requests accept and return `application/json`. Authenticated user endpoints require an `Authorization: Bearer <jwt_token>` header. ESP32 hardware endpoints authenticate using `X-Device-Token` and `X-Device-Key` headers.

---

### Table of Contents
1. [Authentication Endpoints](#1-authentication-endpoints)
2. [Device Management Endpoints](#2-device-management-endpoints)
3. [Remote Command Endpoints](#3-remote-command-endpoints)
4. [ESP32 Hardware Protocol Endpoints](#4-esp32-hardware-protocol-endpoints)
5. [Dashboard & History Endpoints](#5-dashboard--history-endpoints)
6. [System Health & Actuator](#6-system-health--actuator)

---

### 1. Authentication Endpoints

#### 1.1 Register User
- **Method & Path**: `POST /api/auth/register`
- **Access**: Public
- **Request Body**:
```json
{
  "username": "student_dev",
  "email": "student@college.edu",
  "password": "SecurePassword123!"
}
```
- **Response `200 OK`**:
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "id": 3,
    "username": "student_dev",
    "email": "student@college.edu",
    "role": "ROLE_USER",
    "expiresInMs": 86400000
  },
  "timestamp": "2026-10-25T21:40:00"
}
```

#### 1.2 User Login
- **Method & Path**: `POST /api/auth/login`
- **Access**: Public
- **Request Body**:
```json
{
  "username": "admin",
  "password": "Admin@Home2026!"
}
```
- **Response `200 OK`**: Returns JWT Bearer token and user details.
- **Response `401 Unauthorized`**: If credentials do not match.

#### 1.3 Current User Profile
- **Method & Path**: `GET /api/auth/me`
- **Access**: Authenticated (`Bearer <token>`)
- **Response `200 OK`**:
```json
{
  "success": true,
  "message": "Current user profile",
  "data": {
    "id": 1,
    "username": "admin",
    "email": "admin@homeauto.local",
    "role": "ROLE_ADMIN"
  }
}
```

---

### 2. Device Management Endpoints

#### 2.1 Get All Registered Devices
- **Method & Path**: `GET /api/devices`
- **Access**: Authenticated
- **Response `200 OK`**:
```json
{
  "success": true,
  "message": "Devices retrieved successfully",
  "data": [
    {
      "id": 1,
      "deviceKey": "ESP32_RELAY_01",
      "name": "Living Room Light",
      "roomId": 1,
      "roomName": "Living Room",
      "type": "LIGHT",
      "pinNumber": 26,
      "relayChannel": 1,
      "enabled": true,
      "isOnline": true,
      "lastSeenAt": "2026-10-25T21:48:39",
      "confirmedState": "OFF",
      "lastRequestedState": "OFF"
    }
  ]
}
```

#### 2.2 Create New Device
- **Method & Path**: `POST /api/devices`
- **Access**: Authenticated
- **Validation**: `pinNumber` must strictly belong to the safe GPIO whitelist (`4, 5, 12, 13, 14, 15, 16, 17, 18, 19, 21, 22, 23, 25, 26, 27, 32, 33`).
- **Request Body**:
```json
{
  "deviceKey": "ESP32_RELAY_05",
  "name": "Balcony Light",
  "roomId": 1,
  "type": "LIGHT",
  "pinNumber": 18,
  "relayChannel": 5,
  "deviceSecret": "optional_custom_secret"
}
```

#### 2.3 Update Device
- **Method & Path**: `PUT /api/devices/{id}`
- **Access**: Authenticated
- **Request Body**: Accepts updated name, roomId, type, safe pinNumber, relayChannel, and enabled flag.

#### 2.4 Delete Device
- **Method & Path**: `DELETE /api/devices/{id}`
- **Access**: Restricted (`ROLE_ADMIN` only)

---

### 3. Remote Command Endpoints

#### 3.1 Send Command to Device
- **Method & Path**: `POST /api/devices/{id}/commands`
- **Access**: Authenticated
- **Request Body**:
```json
{
  "requestedState": "ON"
}
```
- **Response `200 OK`**:
```json
{
  "success": true,
  "message": "Command queued successfully for hardware dispatch",
  "data": {
    "id": 12,
    "commandUuid": "a87fdc90-48e2-45e0-8fa8-ebcae7284b12",
    "deviceId": 1,
    "deviceKey": "ESP32_RELAY_01",
    "deviceName": "Living Room Light",
    "requestedState": "ON",
    "status": "PENDING",
    "createdAt": "2026-10-25T21:48:40"
  }
}
```

#### 3.2 Toggle Appliance State
- **Method & Path**: `POST /api/devices/{id}/toggle`
- **Access**: Authenticated
- Inverts current confirmed physical state (ON -> OFF or OFF -> ON) and queues command.

#### 3.3 Master All-Off Command
- **Method & Path**: `POST /api/commands/all-off`
- **Access**: Authenticated
- Queues `OFF` command sequence across all enabled appliances.

---

### 4. ESP32 Hardware Protocol Endpoints

#### 4.1 Telemetry Heartbeat
- **Method & Path**: `POST /api/esp32/heartbeat`
- **Headers**: `X-Device-Token: <device_secret>`
- **Request Body**:
```json
{
  "deviceKey": "ESP32_RELAY_01",
  "confirmedState": "OFF",
  "uptimeSeconds": 340,
  "wifiRssi": -62,
  "ipAddress": "192.168.1.155",
  "firmwareVersion": "1.0.0"
}
```
- **Behavior**: Marks device `isOnline = true`, updates `lastSeenAt`, logs IP & RSSI signal telemetry.

#### 4.2 Hardware Command Polling
- **Method & Path**: `GET /api/esp32/poll`
- **Headers**:
  - `X-Device-Key: ESP32_RELAY_01`
  - `X-Device-Token: sec_lr_light_9921`
- **Response `200 OK` (Command Found)**:
```json
{
  "hasPendingCommand": true,
  "commandId": 12,
  "commandUuid": "a87fdc90-48e2-45e0-8fa8-ebcae7284b12",
  "deviceKey": "ESP32_RELAY_01",
  "pinNumber": 26,
  "relayChannel": 1,
  "requestedState": "ON",
  "serverTime": 1729873720000
}
```
- **Response `200 OK` (No Pending Command)**:
```json
{
  "hasPendingCommand": false
}
```

#### 4.3 Command Execution Acknowledgement
- **Method & Path**: `POST /api/esp32/acknowledge`
- **Headers**: `X-Device-Token: <device_secret>`
- **Request Body**:
```json
{
  "commandUuid": "a87fdc90-48e2-45e0-8fa8-ebcae7284b12",
  "deviceKey": "ESP32_RELAY_01",
  "executedState": "ON",
  "success": true,
  "errorMessage": null
}
```
- **Behavior**: Verifies token and command UUID match. Updates command status to `ACKNOWLEDGED`. Updates `confirmedState` in database. Logs audit event `STATE_CHANGED`.

---

### 5. Dashboard & History Endpoints

#### 5.1 Dashboard Summary
- **Method & Path**: `GET /api/dashboard/summary`
- **Access**: Authenticated
- **Response `200 OK`**:
```json
{
  "success": true,
  "message": "Dashboard summary retrieved",
  "data": {
    "totalDevices": 4,
    "onlineDevices": 4,
    "activeDevices": 1,
    "offlineDevices": 0,
    "serverTime": "2026-10-25T21:48:42",
    "devices": [...]
  }
}
```

#### 5.2 Event & Audit History
- **Method & Path**: `GET /api/history`
- **Access**: Authenticated
- **Optional Query Params**: `deviceId`, `startDate`, `endDate`
- **Response `200 OK`**: Returns chronological list of state changes, commands, and telemetry events.

---

### 6. System Health & Actuator

#### 6.1 Basic Health Check
- **Method & Path**: `GET /api/health`
- **Access**: Public
- **Response `200 OK`**:
```json
{
  "status": "UP",
  "application": "IoT-Based Home Automation System using ESP32",
  "uptimeSeconds": 1284,
  "components": {
    "database": "UP",
    "esp32Watchdog": "ACTIVE",
    "security": "ENABLED"
  }
}
```

#### 6.2 Spring Boot Actuator
- `GET /actuator/health`
- `GET /actuator/info`
