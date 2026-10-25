# IoT-Based Home Automation System using ESP32

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21%20%7C%2025%20LTS-orange.svg)](https://adoptium.net)
[![ESP32](https://img.shields.io/badge/Microcontroller-ESP32_DevKit_V1-red.svg)](https://www.espressif.com)
[![MySQL](https://img.shields.io/badge/Database-MySQL_8.x-blue.svg)](https://www.mysql.com)
[![Tests](https://img.shields.io/badge/Tests-14%2F14_PASS-success.svg)](docs/TEST_CASES.md)

A complete, production-grade, college-capstone-quality IoT home automation system. Allows authenticated users to remotely control household lights and fans with verified physical hardware state synchronization, cryptographic device token authentication, active-low relay driving, and automated watchdog monitoring.

---

## 🌟 Key Architecture & Highlights

- **Verified Physical State**: Distinguishes requested state from confirmed physical state. The web switch updates only after the ESP32 acknowledges actuation.
- **Microcontroller Safety**: Strict software whitelist of permitted general-purpose output GPIOs protects the ESP32 against SPI Flash conflicts or boot strapping failures.
- **Fail-Safe Relay Logic**: Configurable Active-Low relay driving (`LOW = ON, HIGH = OFF`) with safe boot sequence (all relays initialized OFF on boot).
- **Background Watchdog**: Monitors heartbeat telemetry every 10 seconds. Flags devices offline if heartbeats lapse past 35s and expires timed-out commands.
- **Digital Twin Simulator**: Includes `simulator/mock_esp32.py` in Python to demonstrate and test the entire vertical slice on Windows without physical hardware.

---

## 📁 Repository Directory Structure

```text
iot-home-automation/
├── backend/                  # Java Spring Boot 3 Maven Web Application
│   ├── pom.xml               # Maven dependencies (Web, Security, JPA, Validation, Actuator, JWT)
│   └── src/
│       ├── main/java/com/homeauto/
│       │   ├── config/       # SecurityConfig, JwtProvider, CorsConfig, DataInitializer
│       │   ├── controller/   # Auth, Device, Esp32, Dashboard, History, Health
│       │   ├── dto/          # Clean DTOs for requests, responses, telemetry
│       │   ├── entity/       # User, Room, Device, Command, DeviceEvent
│       │   ├── repository/   # Spring Data JPA Repositories
│       │   └── service/      # AuthService, DeviceService, CommandService, Esp32Service, Watchdog
│       ├── main/resources/
│       │   ├── application.properties        # MySQL configuration
│       │   ├── application-h2.properties     # Standalone in-memory profile
│       │   └── static/                       # Embedded Web Frontend (HTML5/CSS3/Bootstrap 5/JS)
│       └── test/java/com/homeauto/           # JUnit 5 & Mockito test suite (14 passing tests)
├── frontend/                 # Standalone copy of HTML/CSS/JS frontend
├── firmware/                 # Production C++ Arduino firmware for ESP32
│   └── esp32_home_automation/
│       ├── esp32_home_automation.ino         # Compilable Arduino sketch
│       ├── config.h.example                  # Wi-Fi and backend URL template
│       └── README.md                         # Flashing guide via Arduino IDE
├── simulator/                # Python Hardware Digital Twin Simulator
│   ├── mock_esp32.py         # Full ESP32 emulation script
│   └── run_simulator.bat     # Windows one-click batch runner
├── database/                 # Normalized MySQL 8.x SQL scripts
│   ├── schema.sql            # Table definitions, foreign keys, indexes
│   └── seed.sql              # Development seed data with BCrypt admin credentials
├── docs/                     # Academic Submission Documentation Suite
│   ├── College_Project_Report.md  # 15-section complete academic project report
│   ├── API_DOCUMENTATION.md       # Comprehensive REST API reference
│   ├── WIRING_AND_HARDWARE.md     # BOM, optocoupler wiring & AC safety protocols
│   ├── TEST_CASES.md              # Detailed test matrix with expected vs actual results
│   └── VIVA_QUESTIONS.md          # 13 technical viva questions & answers
├── .env.example              # Safe environment variable configuration template
├── .gitignore
└── README.md                 # Master project documentation (this file)
```

---

## 🚀 Quick Start Guide (Windows)

### Step 1: Clone / Open Project
The project is located at:
```cmd
C:\Users\Bhushan\.gemini\antigravity\scratch\iot-home-automation
```
> **Recommendation**: Set this directory as your active workspace in your IDE (VS Code or IntelliJ IDEA).

---

### Step 2: Database Setup (MySQL 8.x)
Ensure MySQL service is running. Create the database:
```cmd
mysql -u root -p
```
```sql
CREATE DATABASE IF NOT EXISTS home_automation CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT;
```
*(Optional: Run `mysql -u root -p home_automation < database/schema.sql` to preload tables. Spring Boot's Hibernate will also create tables automatically if omitted).*

---

### Step 3: Start the Spring Boot Backend
Open PowerShell in the `backend` directory:
```powershell
cd C:\Users\Bhushan\.gemini\antigravity\scratch\iot-home-automation\backend
mvn spring-boot:run
```
> **Zero-Configuration Offline Mode**: To run without MySQL using the in-memory H2 database:
> ```powershell
> mvn spring-boot:run -Dspring-boot.run.profiles=h2
> ```

The backend starts at: **`http://localhost:8080`**.

---

### Step 4: Open the Web Dashboard
Open your browser to:
👉 **`http://localhost:8080/index.html`**

Log in using the pre-seeded administrator credentials:
- **Username**: `admin`
- **Password**: `Admin@Home2026!`

*(A demo user is also pre-configured: `demouser` / `User@Home2026!`)*

---

### Step 5: Run the ESP32 Hardware Simulator (Digital Twin)
In a new terminal window, start the Python simulator:
```powershell
cd C:\Users\Bhushan\.gemini\antigravity\scratch\iot-home-automation
python simulator/mock_esp32.py
```
*(Or double-click `simulator/run_simulator.bat`)*

**What happens**:
1. The simulator connects to `http://localhost:8080`.
2. All 4 appliance cards on the web dashboard immediately flip to **ONLINE** (green pulse).
3. When you toggle any switch (Living Room Light, Bedroom Fan, etc.), the simulator receives the command in real-time, displays the GPIO actuation in colored terminal output, and returns an authenticated ACK.
4. The dashboard switch confirms the state and records the event in the **Activity History** tab!

---

### Step 6: Connect Physical ESP32 Hardware (When Ready)

1. Open `firmware/esp32_home_automation/esp32_home_automation.ino` in **Arduino IDE**.
2. Copy `config.h.example` to `config.h`.
3. Set your Wi-Fi credentials and your computer's **LAN IP** (e.g. `http://192.168.1.100:8080`).
   > **Note**: Do NOT use `localhost` in the ESP32 code because `localhost` points to the ESP32 chip itself!
4. Wire the 4-channel relay module to ESP32:
   - **IN1** -> **GPIO 26** (Living Room Light)
   - **IN2** -> **GPIO 25** (Bedroom Light)
   - **IN3** -> **GPIO 33** (Living Room Fan)
   - **IN4** -> **GPIO 32** (Bedroom Fan)
   - **VCC** -> **3.3V / 5V**
   - **GND** -> **GND**
5. Select board **DOIT ESP32 DEVKIT V1**, select your COM port, and click **Upload**.

---

## 🧪 Automated Testing

To run the complete automated test suite (unit tests, security tests, and vertical slice integration tests):
```powershell
cd C:\Users\Bhushan\.gemini\antigravity\scratch\iot-home-automation\backend
mvn test
```

**Results**:
```text
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 🛡️ Hardware & High-Voltage Electrical Safety Notice

- **Recommended**: For academic evaluation, use **5V/12V DC loads** (LEDs and DC brushless fans).
- If wiring 230V AC mains appliances:
  - High-voltage mains can cause fatal electric shocks.
  - Never test bare mains wires on a breadboard.
  - Use an enclosed flame-retardant relay box, proper earthing, and line fuses.
  - Work must be verified by a certified electrician.

---

## 📚 College Project Submission Documents

Comprehensive documentation is provided in the `docs/` directory:
- [Academic Project Report](docs/College_Project_Report.md): 15-section thesis with UML diagrams and SRS.
- [REST API Reference](docs/API_DOCUMENTATION.md): Endpoint JSON contracts.
- [Hardware Wiring Guide](docs/WIRING_AND_HARDWARE.md): BOM, pin mappings, and optocoupler isolation.
- [Test Cases Matrix](docs/TEST_CASES.md): Verification test suite with actual results.
- [Viva Voce Questions & Answers](docs/VIVA_QUESTIONS.md): 13 technical defense questions and answers.
