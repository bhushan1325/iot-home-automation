# Complete Railway Deployment Guide
## IoT-Based Home Automation System using ESP32 & Spring Boot 3

Deploying to **Railway** ([railway.com](https://railway.com)) allows your home automation system to run in the cloud with:
- A managed **MySQL 8.x** relational database with automated backups.
- A public **HTTPS** endpoint accessible from anywhere in the world.
- Continuous deployment (auto-redeploy on git push).
- Direct global connectivity for both the **ESP32 microcontroller** and the **Web Dashboard**.

---

## 🏗️ Railway Architecture Overview

```
+-------------------------------------------------------------+
|                     RAILWAY CLOUD PROJECT                   |
|                                                             |
|  +-------------------------+     +-----------------------+  |
|  |   Spring Boot Service   |     |     MySQL Service     |  |
|  |  (Built via Dockerfile) |<--->|     (Managed 8.x)     |  |
|  |   Port: ${PORT}         |     |   Internal Network    |  |
|  +-------------------------+     +-----------------------+  |
|               ^                                             |
|               | Public HTTPS (Port 443)                     |
+---------------+---------------------------------------------+
                |
     +----------+----------+
     |                     |
     v                     v
[ Web Dashboard ]    [ ESP32 Hardware / Simulator ]
 (Any Browser)        (Wi-Fi Internet Connection)
```

---

## 📋 Step-by-Step Deployment Instructions

### Step 1: Push Code to GitHub

1. If you haven't already, push this project to a GitHub repository:
   ```powershell
   cd C:\Users\Bhushan\.gemini\antigravity\scratch\iot-home-automation
   git add .
   git commit -m "feat: configure project for Railway cloud deployment"
   ```
2. Create a new repository on [GitHub](https://github.com/new) named `iot-home-automation`.
3. Link and push your local branch:
   ```powershell
   git remote add origin https://github.com/<YOUR_GITHUB_USERNAME>/iot-home-automation.git
   git branch -M main
   git push -u origin main
   ```

---

### Step 2: Create a New Project on Railway

1. Go to [railway.com](https://railway.com) and log in with your GitHub account.
2. Click **"+ New Project"**.
3. Select **"Deploy from GitHub repo"**.
4. Choose your `iot-home-automation` repository.
5. Railway will automatically detect the root `Dockerfile` and begin the Maven multi-stage build.

---

### Step 3: Add Managed MySQL Database

1. In the same Railway project canvas, click the **"+ New"** button (top right).
2. Select **"Database"** $\rightarrow$ **"Add MySQL"**.
3. Railway will provision a dedicated MySQL 8.x container in a few seconds.

---

### Step 4: Connect the Database to Spring Boot

Railway automatically isolates services on a private internal network. To link MySQL to your Spring Boot application:

1. Click on your **Web Service** card in the Railway dashboard.
2. Go to the **"Variables"** tab.
3. Click **"+ New Variable"** (or **"Add Reference"**):
   - `SPRING_PROFILES_ACTIVE`: `mysql`
   - `MYSQLHOST`: `${{MySQL.MYSQLHOST}}`
   - `MYSQLPORT`: `${{MySQL.MYSQLPORT}}`
   - `MYSQLUSER`: `${{MySQL.MYSQLUSER}}`
   - `MYSQLPASSWORD`: `${{MySQL.MYSQLPASSWORD}}`
   - `MYSQLDATABASE`: `${{MySQL.MYSQLDATABASE}}`
   - `JWT_SECRET`: `404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970`
4. Click **Deploy**. Railway will restart the container with the MySQL profile activated.
   Hibernate will automatically create all tables (`users`, `rooms`, `devices`, `commands`, `device_events`) and seed the default administrator account!

---

### Step 5: Generate Public HTTPS Domain

1. In your **Web Service**, go to **"Settings"**.
2. Scroll to the **"Networking"** section.
3. Under **Public Networking**, click **"Generate Domain"**.
4. Railway will assign a public HTTPS domain:
   ```
   https://iot-home-automation-production.up.railway.app
   ```

---

### Step 6: Test the Live Dashboard

Open your browser to:
👉 **`https://your-app-domain.up.railway.app/index.html`**

Sign in with default administrator credentials:
- **Username**: `admin`
- **Password**: `Admin@Home2026!`

*(You can also test the health check: `https://your-app-domain.up.railway.app/api/health`)*

---

### Step 7: Connect the Digital Twin Simulator to Railway

You can run the Python hardware simulator on Windows against your live Railway cloud deployment:

```powershell
cd C:\Users\Bhushan\.gemini\antigravity\scratch\iot-home-automation
python simulator\mock_esp32.py https://your-app-domain.up.railway.app
```

**Outcome**:
- The simulator establishes an authenticated HTTPS connection to Railway.
- All appliance cards on the cloud dashboard immediately pulse green (**ONLINE**).
- Toggling a button on your phone or PC over the public internet sends commands through Railway down to the simulator in real-time!

---

### Step 8: Configure Physical ESP32 Hardware for Railway

1. Open `firmware/esp32_home_automation/config.h` in **Arduino IDE**.
2. Update `BACKEND_BASE_URL` to point to your Railway domain:
   ```cpp
   const char* BACKEND_BASE_URL = "https://your-app-domain.up.railway.app";
   ```
3. The firmware sketch automatically uses `WiFiClientSecure` with `.setInsecure()` to handle Railway's Cloudflare / Let's Encrypt TLS handshake without needing certificate thumbprints.
4. Upload the sketch to your ESP32 board.
5. The ESP32 will now connect to Railway from any Wi-Fi network (home, college campus, mobile hotspot) without requiring port forwarding or local LAN routing!
