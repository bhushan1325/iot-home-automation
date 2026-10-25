# Viva Voce Questions & Answers
## IoT-Based Home Automation System using ESP32

---

### Category 1: Hardware & Embedded Systems (ESP32)

#### Q1: What microcontroller is used in this project and what are its key specifications?
**Answer**:  
The system uses the **ESP32 DevKit V1 (ESP-WROOM-32)** manufactured by Espressif Systems. Key specifications include:
- Dual-core 32-bit Xtensa LX6 microprocessor running at 240 MHz.
- Integrated 2.4 GHz 802.11 b/g/n Wi-Fi and Bluetooth v4.2 / BLE.
- 520 KB internal SRAM and 4 MB external SPI Flash.
- 36 GPIO pins with capacitive touch, 12-bit ADCs, DACs, SPI, I2C, and PWM.
- Operating logic voltage: 3.3V DC.

#### Q2: Why are certain GPIO pins restricted in software? Which pins cannot be used for relays?
**Answer**:  
- **Pins 6 - 11**: Connected directly to the integrated SPI flash chip. Attempting to use these pins halts program execution and triggers an immediate boot loop.
- **Pins 34, 35, 36 (VP), and 39 (VN)**: Input-only pins that lack output driver circuitry; they cannot produce voltage to drive relays.
- **Pins 0, 2, 12, and 15**: Strapping pins that dictate chip bootloader modes and flash voltage on bootup. Floating or grounding them improperly causes boot failures.  
Our software enforces a strict whitelist of safe output pins (e.g. GPIO 26, 25, 33, 32).

#### Q3: What is "Active-Low" relay logic and why do most 5V relay breakout boards use it?
**Answer**:  
In an Active-Low relay module:
- A digital **LOW (0V)** signal turns the relay **ON** (current flows from VCC through the internal optocoupler LED to ground).
- A digital **HIGH (3.3V or 5V)** turns the relay **OFF**.  
This design is popular in embedded systems because NPN driver transistors or microcontrollers can sink current to ground more efficiently than sourcing current.

#### Q4: Why is optocoupler isolation essential between the ESP32 and relay coils?
**Answer**:  
Electromechanical relay coils are inductive loads. When de-energized, the collapsing magnetic field creates a high-voltage back-EMF spike (flyback voltage). Optocouplers provide galvanic isolation via infrared optical transfer, ensuring that electrical noise, high-voltage spikes, and coil current surges never reach the sensitive 3.3V ESP32 microcontroller core.

#### Q5: Can standard relays control fan speed? How is safe fan control achieved?
**Answer**:  
No. Electromechanical relays are binary ON/OFF devices. Rapidly switching them with Pulse Width Modulation (PWM) causes high-frequency mechanical contact arcing, welding the contacts shut and destroying the relay. Safe speed control requires a TRIAC with a zero-crossing detector (for AC fans) or a MOSFET driver (for DC fans). In this project, fans are safely controlled in ON/OFF mode, which is accurately reflected on the dashboard.

---

### Category 2: Backend Architecture & Spring Boot

#### Q6: Explain the architectural pattern of the Java Spring Boot backend.
**Answer**:  
The backend follows a **layered, separation-of-concerns architecture**:
1. **Controller Layer** (`@RestController`): Handles incoming HTTP requests, performs parameter validation (`@Valid`), and returns uniform JSON responses (`ApiResponse<T>`).
2. **Service Layer** (`@Service`): Contains business logic, command state transitions, security checks, and `@Transactional` boundaries.
3. **Repository Layer** (`@Repository`): Interfaces extending `JpaRepository` using Hibernate to interact with MySQL without boilerplate SQL.
4. **Entity Layer** (`@Entity`): Java representations of normalized MySQL relational tables.
5. **Security Layer**: Spring Security filter chain enforcing stateless JWT authentication and role-based authorization.

#### Q7: How does the backend prevent duplicate command execution?
**Answer**:  
When a user submits a command for a device, `CommandService.submitCommand()` queries for any existing `PENDING` commands for that device, marks them as `FAILED` (with error message `"Superseded by new command"`), and generates a new unique UUID for the incoming command. This ensures the ESP32 never processes conflicting or stale command queues.

#### Q8: How does the system handle an offline ESP32 and avoid false state reporting?
**Answer**:  
The system differentiates between **requested state** and **confirmed physical state**:
1. When a user flips a web switch, only the `lastRequestedState` changes; `confirmedState` remains unchanged until the ESP32 acknowledges physical relay actuation.
2. The `DeviceWatchdogService` runs periodically in the background. If an ESP32 misses heartbeats beyond 35 seconds, it marks the device `isOnline = false` and logs an `OFFLINE_DETECTED` event.
3. The dashboard clearly warns: *"Hardware unreachable. Showing last known state."*

---

### Category 3: Database & Security

#### Q9: How are user passwords secured in MySQL?
**Answer**:  
Passwords are never stored in plaintext. They are hashed using **BCrypt** with an adaptive work factor (salt rounds = 10). BCrypt incorporates a random 128-bit salt and is mathematically resistant to rainbow table and brute-force attacks.

#### Q10: How are ESP32 microcontrollers authenticated to prevent unauthorized control?
**Answer**:  
Each device is issued a distinct `deviceSecret` token stored in MySQL. When the ESP32 sends a heartbeat, polls commands, or acknowledges execution, it passes `X-Device-Token` and `X-Device-Key` headers. The `Esp32Service` validates that the token matches the specific device record before fulfilling any request, preventing unauthorized or spoofed hardware actuation.

#### Q11: Explain the database normalization level of this system.
**Answer**:  
The schema is normalized to **Third Normal Form (3NF)** and **Boyce-Codd Normal Form (BCNF)**:
- **1NF**: All columns contain atomic values, and each table has a primary key (`id`).
- **2NF**: No partial functional dependencies; all non-key attributes are fully dependent on the entire primary key.
- **3NF**: No transitive dependencies; room metadata is isolated in the `rooms` table, device parameters in `devices`, execution logs in `commands`, and telemetry history in `device_events`.

---

### Category 4: Networking & Protocols

#### Q12: Why was authenticated HTTP polling selected over MQTT or WebSockets for this implementation?
**Answer**:  
1. **Firewall and NAT Traversal**: HTTP polling works over standard port 80/8080 without requiring custom broker ports (e.g. 1883 for MQTT) to be opened on college or home Wi-Fi routers.
2. **Deterministic Acknowledgement**: Every command lifecycle (Pending -> Dispatched -> Acknowledged) has an explicit transactional database receipt.
3. **Simplicity and Reliability**: It eliminates third-party broker dependencies (e.g. Mosquitto), keeping the deployment self-contained with Spring Boot and MySQL.

#### Q13: Why can't the ESP32 connect to "http://localhost:8080"?
**Answer**:  
`localhost` (loopback address `127.0.0.1`) refers to the local device itself. On the ESP32, `localhost` points to the ESP32 chip, not the host computer running Spring Boot. The ESP32 must use the computer's **Local Area Network (LAN) IP address** (e.g., `http://192.168.1.100:8080`) assigned by the Wi-Fi router.
