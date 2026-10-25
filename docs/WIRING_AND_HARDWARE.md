# Hardware Architecture, Wiring & Electrical Safety Guide
## IoT-Based Home Automation System using ESP32

---

## 1. Bill of Materials (BOM)

| Item # | Component | Model / Specification | Quantity | Primary Function |
|---|---|---|---|---|
| 1 | **Microcontroller Board** | ESP32 DevKit V1 (30-pin or 38-pin WROOM-32) | 1 | Wi-Fi client, REST polling, relay GPIO actuation |
| 2 | **Relay Module** | 4-Channel 5V Relay Breakout with Optocoupler Isolation | 1 | Physical electrical switching between low-voltage logic & loads |
| 3 | **Microcontroller Power Supply** | 5V 2.0A Micro-USB or regulated switch-mode power adapter | 1 | Powers ESP32 logic and onboard 3.3V LDO regulator |
| 4 | **Auxiliary Relay Power Supply** | Regulated 5V DC 1.0A power source (for JD-VCC) | 1 | Powers electromagnetic coil current (avoids brownout on ESP32) |
| 5 | **Demonstration Loads (Recommended)** | 5V/12V DC LED modules & 5V DC miniature brushless fans | 4 | Low-voltage, 100% safe physical demonstration loads |
| 6 | **Prototyping Wires** | Male-to-Female & Male-to-Male DuPont jumper wires | 15-20 | Signal and ground interconnections |
| 7 | **Solderless Breadboard** | 400-point or 830-point half-size breadboard | 1 | Mounting ESP32 DevKit and power rail distribution |

---

## 2. GPIO Pin Assignment & Rationale

ESP32 GPIO pins have various internal multiplexing constraints (strapping pins, input-only pins, integrated flash pins). The selected pins below have been verified safe for relay output:

| Channel | Appliance Controlled | ESP32 GPIO | Default Boot Level | Trigger Logic | Safe Output Rationale |
|---|---|---|---|---|---|
| **IN1** | Living Room Light | **GPIO 26** | LOW (Floating) | Active-LOW (0V = ON) | Safe ADC2 / DAC2 pin; no boot strapping interference |
| **IN2** | Bedroom Light | **GPIO 25** | LOW (Floating) | Active-LOW (0V = ON) | Safe ADC2 / DAC1 pin; no boot strapping interference |
| **IN3** | Living Room Fan | **GPIO 33** | LOW (Floating) | Active-LOW (0V = ON) | Safe ADC1 pin; safe general-purpose output |
| **IN4** | Bedroom Fan | **GPIO 32** | LOW (Floating) | Active-LOW (0V = ON) | Safe ADC1 pin; safe general-purpose output |

### Strict GPIO Constraints Enforced by Software:
- **GPIO 6 - 11**: Connected internally to SPI Flash memory. Configuring these as output causes instantaneous kernel panics and boot loops.
- **GPIO 34 - 39**: Input-only pins (lack output driver FETs). Cannot be used to trigger relays.
- **GPIO 0, 2, 12, 15**: Strapping pins used by ESP32 bootloader to configure flash voltage and boot mode.

---

## 3. Optocoupler Isolation & JD-VCC Power Wiring

Most 4-channel 5V relay boards feature a **JD-VCC / VCC jumper**:
1. When the jumper is attached, the ESP32 5V rail powers both the optocoupler LEDs and the physical relay coils. When multiple relays click simultaneously, the sudden coil inrush current (~70 mA per relay = 280 mA total) can cause voltage dips, triggering an ESP32 brownout reset!
2. **Recommended Isolated Configuration**:
   - Remove the `JD-VCC` jumper cap.
   - Connect **ESP32 3.3V (or 5V)** to the relay board **VCC** pin (powers the input optocoupler phototransistors).
   - Connect an external **5V power supply (+)** to **JD-VCC**.
   - Connect external **5V power supply (-)** to **GND** on the relay board.
   - Do **NOT** share GND between ESP32 and relay coil when full galvanic isolation is required.

---

## 4. Connection Diagram

```
+-------------------------------------------------------------+
|                      ESP32 DEVKIT V1                        |
|                                                             |
|   [GPIO 26] ------------------------> IN1 (Relay Module)    |
|   [GPIO 25] ------------------------> IN2 (Relay Module)    |
|   [GPIO 33] ------------------------> IN3 (Relay Module)    |
|   [GPIO 32] ------------------------> IN4 (Relay Module)    |
|   [3.3V / 5V] ----------------------> VCC (Relay Board)     |
|   [GND] ----------------------------> GND (Relay Logic GND) |
+-------------------------------------------------------------+
                               |
                               v
+-------------------------------------------------------------+
|               4-CHANNEL 5V RELAY BOARD (OPTO)               |
|                                                             |
|   Relay 1 [NO] [COM] -----> [ DC Load 1: Living Room Light] |
|   Relay 2 [NO] [COM] -----> [ DC Load 2: Bedroom Light]     |
|   Relay 3 [NO] [COM] -----> [ DC Load 3: Living Room Fan]   |
|   Relay 4 [NO] [COM] -----> [ DC Load 4: Bedroom Fan]       |
+-------------------------------------------------------------+
```

---

## 5. Fan Speed Control Technical Note

- **Electromechanical Relays**: Relays are binary on/off switching devices consisting of physical mechanical contacts. They **cannot** perform continuous speed control (dimming/speed regulation) because switching them rapidly via PWM destroys the relay contacts within minutes due to inductive arcing.
- **Safe Speed Control Architecture**: To implement fan speed regulation safely on AC or DC fans:
  - For **DC Fans**: A MOSFET / H-Bridge driver with hardware PWM on ESP32 (`ledcSetup` / `ledcWrite`) is required.
  - For **AC Fans**: A zero-crossing detector coupled with a TRIAC optocoupler circuit (e.g. MOC3021 + BT136) is mandatory.
- **Project Implementation**: In this project, fans are actuated using high-grade isolated relays strictly adhering to reliable **ON/OFF** control. The web dashboard honestly indicates *"Relay Actuated (Safe ON/OFF logic)"* rather than claiming unsupported hardware speed modulation.

---

## 6. High-Voltage AC Electrical Safety Protocol

> [!CAUTION]
> **230V / 110V Mains Voltage Hazard**  
> Direct exposure to AC mains electricity can cause severe electric shock, serious burns, or electrocution.

1. **Academic Demonstration Best Practice**: Use 5V or 12V DC loads (LED indicators and 5V DC fans) powered by a regulated bench power supply for viva and laboratory demonstrations.
2. If connecting AC mains appliances for permanent installation:
   - All high-voltage connections must be housed inside an **insulated, flame-retardant electrical enclosure** (IP65 rated).
   - Use appropriately rated **fast-acting fuses or miniature circuit breakers (MCBs)** before the relay common terminals.
   - Ensure solid earth grounding for metallic chassis.
   - All mains installation work must be verified by a certified electrician. Never touch bare terminals while power is applied.
