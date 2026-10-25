# System Verification & Test Cases Document
## IoT-Based Home Automation System using ESP32

---

### Test Execution Summary
- **Test Framework**: JUnit 5, Mockito, Spring Boot Test, Spring Security Test, MockMvc
- **Automated Test Results**: **14 Tests Run, 0 Failures, 0 Errors, 0 Skipped (100% Pass Rate)**
- **Java Platform**: OpenJDK 25.0.1 LTS
- **Build Tool**: Apache Maven 3.9.16

---

### Detailed Test Cases Matrix

| Test ID | Module / Feature | Test Objective | Test Steps / Input | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|---|
| **TC-SEC-01** | Authentication | Validate registration of new user | `POST /api/auth/register` with `{ username: "newuser", email: "new@test.com", password: "Password123!" }` | `200 OK`, BCrypt hashed in DB, valid JWT Bearer token returned | User registered; JWT returned with role `ROLE_USER` | **PASS** |
| **TC-SEC-02** | Authentication | Validate rejection of duplicate username | `POST /api/auth/register` with existing username `testuser` | `400 Bad Request`, message `"Username is already taken"` | Rejected with status 400 and descriptive message | **PASS** |
| **TC-SEC-03** | Authentication | Verify user login with valid credentials | `POST /api/auth/login` with `testuser` and `SecretPass123!` | `200 OK`, JWT token generated with 24h validity | Login successful; JWT returned | **PASS** |
| **TC-SEC-04** | Authentication | Verify rejection of incorrect password | `POST /api/auth/login` with `testuser` and `WrongPass` | `401 Unauthorized`, message `"Invalid username or password"` | 401 Unauthorized returned; Security context clean | **PASS** |
| **TC-SEC-05** | Authorization | Protected endpoint access control | `GET /api/devices` without `Authorization` header | `401 Unauthorized` | 401 Unauthorized returned by `JwtAuthenticationFilter` | **PASS** |
| **TC-DEV-01** | Device Mgmt | Register device with safe GPIO 26 | `DeviceService.createDevice()` with pin 26 (Relay CH1) | Device entity persisted; Initial audit log saved | Device created with GPIO 26; audit event logged | **PASS** |
| **TC-DEV-02** | Device Security | Reject registration with unsafe SPI Flash pin 6 | `DeviceService.createDevice()` with pin 6 | `IllegalArgumentException: Invalid or unsafe GPIO pin: 6` | Creation halted; hardware crash prevented | **PASS** |
| **TC-DEV-03** | Device Security | Reject registration with input-only pin 36 | `DeviceService.createDevice()` with pin 36 | `IllegalArgumentException: Invalid or unsafe GPIO pin: 36` | Rejected; cannot drive relays with input-only pin | **PASS** |
| **TC-DEV-04** | Device Mgmt | Reject duplicate deviceKey | Attempt to register existing `ESP32_RELAY_01` | `IllegalArgumentException: Device key already exists` | Rejected with clear exception | **PASS** |
| **TC-CMD-01** | Command Engine | Queue valid command and cancel older pending | Submit `ON` command for device 1 while old `OFF` pending | Old pending command marked `FAILED` (Superseded); new `PENDING` command created | Old command superseded; new command queued | **PASS** |
| **TC-CMD-02** | Command Engine | Block commands to disabled device | Set `device.enabled = false` and submit `ON` command | `IllegalStateException: Device is disabled` | Rejected; no command queued | **PASS** |
| **TC-CMD-03** | Command Engine | Master All-Off sequence execution | Trigger `CommandService.submitAllOff()` | All enabled devices receive `OFF` command entries | Dispatched to all 4 default channels | **PASS** |
| **TC-ESP-01** | ESP32 Protocol | Hardware heartbeat telemetry update | `POST /api/esp32/heartbeat` with RSSI -65, IP 192.168.1.150 | `isOnline = true`, `lastSeenAt` updated, `ONLINE_DETECTED` logged | Device marked online; telemetry logged | **PASS** |
| **TC-ESP-02** | ESP32 Protocol | Reject unauthorized device token | `POST /api/esp32/heartbeat` with token `"WRONG_SECRET"` | `AccessDeniedException` (403 Forbidden) | Hardware rejected; access denied logged | **PASS** |
| **TC-E2E-01** | Vertical Slice | End-to-end command flow: Web -> DB -> ESP32 -> Ack -> DB | 1. User queues `ON`<br>2. ESP32 polls<br>3. ESP32 acknowledges | Command marked `ACKNOWLEDGED`; `device.confirmedState` updated to `ON` | Full lifecycle verified; DB reflects confirmed ON state | **PASS** |
| **TC-WDG-01** | Watchdog | Detect missed heartbeats (>35s) | Simulate time elapsed past threshold without heartbeat | Device marked `isOnline = false`; `OFFLINE_DETECTED` event generated | Watchdog flags device offline; dashboard shows last known state | **PASS** |
| **TC-WDG-02** | Watchdog | Detect command timeout (>20s) | Command in `SENT` state without ACK for 20s | Command status set to `TIMED_OUT`; error event recorded | Command marked TIMED_OUT; hardware failure logged | **PASS** |
