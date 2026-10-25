# 🎓 College Project Submission Guide (5-Minute Setup)
## Project: IoT-Based Home Automation System using ESP32

This guide is specially made for **quick flashing on a borrowed laptop in 5–10 minutes**.  
Once flashed, the laptop is **never needed again**. You can operate and demonstrate the entire project tomorrow using **only your mobile phone**!

---

## 📱 PART 1: Hotspot Configuration (On Your Mobile Phone)

Before touching the laptop, set up your phone hotspot so the ESP32 knows how to connect to the internet:

1. On your phone, go to **Settings** $\rightarrow$ **Portable Hotspot / Personal Hotspot**.
2. Note your exact:
   - **Hotspot Name (SSID)** (e.g. `MyPhone`)
   - **Hotspot Password** (e.g. `12345678`)
3. **IMPORTANT**: Ensure the AP Band is set to **2.4 GHz**:
   - In hotspot settings, choose **2.4 GHz Band** (or turn on **"Extend Compatibility"** if on iPhone).  
   *(ESP32 connects to 2.4 GHz Wi-Fi).*

---

## 💻 PART 2: What to Do on Your Friend's Laptop (5 Minutes)

### Step 1: Download the Project
1. Open Google Chrome on the laptop.
2. Go to your GitHub repository:  
   👉 **https://github.com/bhushan1325/iot-home-automation**
3. Click the green **Code** button $\rightarrow$ click **Download ZIP**.
4. Right-click the downloaded `.zip` file and select **Extract All...** to extract it to the Desktop.

---

### Step 2: Open Arduino IDE & Install 2 Packages
1. If Arduino IDE is not installed, download the official installer (takes 1 minute):  
   👉 **https://www.arduino.cc/en/software**
2. Open Arduino IDE:
   - Go to **File** $\rightarrow$ **Preferences**. In the box labeled **"Additional Boards Manager URLs"**, paste this link:
     ```text
     https://raw.githubusercontent.com/espressif/arduino-esp32/gh-pages/package_esp32_index.json
     ```
     Click **OK**.
   - Go to **Tools** $\rightarrow$ **Board** $\rightarrow$ **Boards Manager...**, search for `esp32` (by Espressif Systems) and click **Install**.
   - Go to **Tools** $\rightarrow$ **Manage Libraries...**, search for `ArduinoJson` (by Benoit Blanchon) and click **Install**.

---

### Step 3: Enter Your Hotspot & Website Link
1. In the extracted folder on Desktop, go to:  
   `iot-home-automation/firmware/esp32_home_automation/`
2. Open the file **`config.h`** (or rename `config.h.example` to `config.h`) in Notepad or Arduino IDE.
3. Edit the top lines with your phone's hotspot and your Railway cloud website link:
   ```cpp
   // 1. Enter your exact phone hotspot details:
   const char* WIFI_SSID     = "PUT_YOUR_HOTSPOT_NAME_HERE";
   const char* WIFI_PASSWORD = "PUT_YOUR_HOTSPOT_PASSWORD_HERE";

   // 2. Enter your live Railway website URL (from your brother):
   const char* BACKEND_BASE_URL = "https://your-project.up.railway.app";
   ```
4. Save the file (**Ctrl + S**).

---

### Step 4: Plug In & Upload the Code
1. Plug the ESP32 into the laptop using a Micro-USB data cable.
2. In Arduino IDE:
   - Go to **File** $\rightarrow$ **Open** $\rightarrow$ select `esp32_home_automation.ino` from the folder.
   - Go to **Tools** $\rightarrow$ **Board** $\rightarrow$ **ESP32 Arduino** $\rightarrow$ select **DOIT ESP32 DEVKIT V1**.
   - Go to **Tools** $\rightarrow$ **Port** $\rightarrow$ select the active COM port (e.g. `COM3`, `COM4`, etc.).
3. Click the **Upload** arrow button at the top left.
   - *Note*: If the console at the bottom shows `Connecting........_____`, press and hold the tiny **BOOT** button on the ESP32 board for 2 seconds until the percentage upload starts.
4. When you see **"Done uploading"**, you are **100% finished with the laptop!**  
   You can unplug the ESP32 and return the laptop to your friend.

---

## 🏛️ PART 3: Tomorrow at College (Mobile-Only Presentation)

When it is your turn to present to the professors:

1. **Turn ON your phone's Mobile Hotspot**.
2. **Power the ESP32 model**:
   - Plug the ESP32's USB cable into any standard **mobile power bank** or phone charger.
   - Within 5 seconds, the ESP32 automatically connects to your phone's hotspot.
3. **Open the Website on Your Phone**:
   - Open Google Chrome or Safari on your phone.
   - Go to your website link:  
     👉 **https://your-project.up.railway.app/index.html**
   - Log in with credentials:
     - **Username**: `admin`
     - **Password**: `Admin@Home2026!`
4. **Demonstrate to the Examiner**:
   - Show the examiner your phone screen: the **Living Room Fan** and **Living Room Light** cards will show a green dot: 🟢 **ONLINE**.
   - Tap the switch on your phone screen:
     - **The physical fan on your table starts spinning immediately!**
     - Tap it again $\rightarrow$ The physical fan stops!
   - Tap the **"Activity History"** tab on your phone:
     - Show the professors the live audit log showing the exact time, device state change, and confirmed hardware acknowledgement!

---

## ❓ Frequently Asked Questions (Viva Cheat Sheet)

- **Q: How does the mobile talk to the ESP32?**  
  *A: The mobile phone talks to a secure cloud REST API hosted on Railway. The ESP32 continuously polls the server over Wi-Fi using authenticated HTTP tokens, executes the relay switch, and sends back physical confirmation.*
- **Q: Does the website update if the fan is disconnected?**  
  *A: No! The system uses pessimistic verification. The web dashboard only confirms ON/OFF when the physical hardware sends an acknowledgement back to the database.*
- **Q: What database is used?**  
  *A: Normalized MySQL 8.x storing users, rooms, devices, commands, and audit logs.*
