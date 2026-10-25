# ESP32 Firmware Guide: IoT-Based Home Automation

This directory contains the production C++ Arduino sketch for the **ESP32 DevKit V1 (30-pin / 38-pin WROOM-32)** microcontroller board connected to a 4-Channel 5V Relay Module.

---

## 1. Prerequisites in Arduino IDE

1. **Install Arduino IDE** (Version 2.x recommended): [https://www.arduino.cc/en/software](https://www.arduino.cc/en/software)
2. **Add ESP32 Board Package**:
   - Open Arduino IDE -> **File** -> **Preferences**.
   - In "Additional Boards Manager URLs", add:
     ```
     https://raw.githubusercontent.com/espressif/arduino-esp32/gh-pages/package_esp32_index.json
     ```
   - Go to **Tools** -> **Board** -> **Boards Manager**, search for `esp32` by Espressif Systems, and click **Install**.
3. **Install Required Libraries**:
   - Go to **Tools** -> **Manage Libraries...**
   - Search for **ArduinoJson** (by Benoit Blanchon, install version **6.x** or **7.x**).
   - `WiFi.h` and `HTTPClient.h` are already built into the ESP32 core package.

---

## 2. Configuration (`config.h`)

1. In this directory, copy the template:
   ```cmd
   copy config.h.example config.h
   ```
2. Open `config.h` and configure:
   - `WIFI_SSID` & `WIFI_PASSWORD`: Your 2.4 GHz Wi-Fi credentials.
   - `BACKEND_BASE_URL`: The local network IP of your host machine running Spring Boot:
     ```cpp
     // IMPORTANT: Do NOT use localhost. Use your computer's LAN IP address!
     const char* BACKEND_BASE_URL = "http://192.168.1.100:8080";
     ```
   - `RELAY_ACTIVE_LOW`: Most 5V relay breakout boards trigger on `LOW` voltage. Keep `true` for active-low boards.

---

## 3. Hardware Pin Mapping

| Relay Channel | Appliance Name | ESP32 GPIO Pin | Trigger Level |
|---|---|---|---|
| Channel 1 (IN1) | Living Room Light | **GPIO 26** | Active-LOW (0V = Relay ON) |
| Channel 2 (IN2) | Bedroom Light | **GPIO 25** | Active-LOW (0V = Relay ON) |
| Channel 3 (IN3) | Living Room Fan | **GPIO 33** | Active-LOW (0V = Relay ON) |
| Channel 4 (IN4) | Bedroom Fan | **GPIO 32** | Active-LOW (0V = Relay ON) |

---

## 4. Flashing the Firmware

1. Connect the ESP32 board to your PC via a Micro-USB data cable (ensure it is a data cable, not charge-only).
2. In Arduino IDE:
   - **Tools** -> **Board** -> **ESP32 Arduino** -> **DOIT ESP32 DEVKIT V1**
   - **Tools** -> **Port** -> Select your COM port (e.g. `COM3` or `COM4`).
   - **Tools** -> **Upload Speed** -> `115200` (or `921600`).
3. Click **Upload** (Arrow icon).
   - If the console shows `Connecting........_____`, press and hold the **BOOT** button on the ESP32 board for 2 seconds until uploading begins.
4. Open the **Serial Monitor** at **115200 baud** to monitor Wi-Fi association and real-time polling telemetry.
