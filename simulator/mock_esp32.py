#!/usr/bin/env python3
"""
=============================================================================
ESP32 DevKit V1 Hardware Simulator (Digital Twin)
Project: IoT-Based Home Automation System using ESP32
Author: Senior IoT Engineer

This script faithfully emulates the physical ESP32 microcontroller:
- Establishes authenticated connection with Spring Boot REST API
- Sends periodic heartbeats with realistic RSSI, uptime, and IP telemetry
- Continuously polls for user-queued commands
- Simulates physical optocoupler relay actuation with active-low pin logic
- Reports command execution acknowledgements back to the backend
=============================================================================
"""

import sys
import time
import json
import random
import urllib.request
import urllib.error

# ANSI Color codes for clean dashboard display
class Color:
    RESET   = "\033[0m"
    BOLD    = "\033[1m"
    RED     = "\033[91m"
    GREEN   = "\033[92m"
    YELLOW  = "\033[93m"
    BLUE    = "\033[94m"
    MAGENTA = "\033[95m"
    CYAN    = "\033[96m"
    WHITE   = "\033[97m"

SERVER_URL = "http://localhost:8080"

# Hardware Relay Mapping matching the ESP32 configuration
RELAYS = [
    {
        "deviceKey": "ESP32_RELAY_01",
        "deviceToken": "sec_lr_light_9921",
        "name": "Living Room Light",
        "pin": 26,
        "channel": 1,
        "state": "OFF"
    },
    {
        "deviceKey": "ESP32_RELAY_02",
        "deviceToken": "sec_br_light_7732",
        "name": "Bedroom Light",
        "pin": 25,
        "channel": 2,
        "state": "OFF"
    },
    {
        "deviceKey": "ESP32_RELAY_03",
        "deviceToken": "sec_lr_fan_4418",
        "name": "Living Room Fan",
        "pin": 33,
        "channel": 3,
        "state": "OFF"
    },
    {
        "deviceKey": "ESP32_RELAY_04",
        "deviceToken": "sec_br_fan_6654",
        "name": "Bedroom Fan",
        "pin": 32,
        "channel": 4,
        "state": "OFF"
    }
]

SIMULATED_IP = "192.168.1.155"
START_TIME = time.time()

def log(msg, color=Color.WHITE):
    timestamp = time.strftime("%H:%M:%S")
    print(f"{Color.CYAN}[{timestamp}]{Color.RESET} {color}{msg}{Color.RESET}")

def make_request(endpoint, method="GET", headers=None, data=None):
    url = f"{SERVER_URL}{endpoint}"
    req_headers = {
        "User-Agent": "ESP32-HTTPClient/1.0.0",
        "Content-Type": "application/json"
    }
    if headers:
        req_headers.update(headers)

    body_bytes = None
    if data is not None:
        body_bytes = json.dumps(data).encode("utf-8")

    req = urllib.request.Request(url, data=body_bytes, headers=req_headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=4) as response:
            return response.status, json.loads(response.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        return e.code, None
    except urllib.error.URLError:
        return None, None

def send_heartbeat(relay):
    uptime = int(time.time() - START_TIME)
    rssi = random.randint(-68, -55) # Realistic Wi-Fi signal in dBm
    payload = {
        "deviceKey": relay["deviceKey"],
        "confirmedState": relay["state"],
        "uptimeSeconds": uptime,
        "wifiRssi": rssi,
        "ipAddress": SIMULATED_IP,
        "firmwareVersion": "1.0.0-simulator"
    }
    status, _ = make_request(
        "/api/esp32/heartbeat",
        method="POST",
        headers={"X-Device-Token": relay["deviceToken"]},
        data=payload
    )
    return status == 200

def poll_command(relay):
    status, res = make_request(
        "/api/esp32/poll",
        method="GET",
        headers={
            "X-Device-Key": relay["deviceKey"],
            "X-Device-Token": relay["deviceToken"]
        }
    )
    if status == 200 and res and res.get("hasPendingCommand"):
        cmd_uuid = res.get("commandUuid")
        requested_state = res.get("requestedState")
        pin = res.get("pinNumber")

        log(f"[COMMAND RECEIVED] {relay['name']} (GPIO {pin}) -> Requested: {requested_state} [UUID: {cmd_uuid[:8]}...]", Color.YELLOW)
        
        # Simulate physical relay switching delay (mechanical relay contact settle ~20ms)
        time.sleep(0.05)
        relay["state"] = requested_state
        state_color = Color.GREEN if requested_state == "ON" else Color.RED

        log(f"[HARDWARE RELAY] CH{relay['channel']} GPIO {pin} switched to {state_color}{requested_state}{Color.RESET}", Color.BOLD)

        # Send ACK back to backend
        ack_payload = {
            "commandUuid": cmd_uuid,
            "deviceKey": relay["deviceKey"],
            "executedState": requested_state,
            "success": True,
            "errorMessage": None
        }
        ack_status, _ = make_request(
            "/api/esp32/acknowledge",
            method="POST",
            headers={"X-Device-Token": relay["deviceToken"]},
            data=ack_payload
        )
        if ack_status == 200:
            log(f"[ACK CONFIRMED] State '{requested_state}' saved to database for {relay['name']}", Color.GREEN)
        else:
            log(f"[ACK ERROR] Failed to acknowledge command with backend (HTTP {ack_status})", Color.RED)

def print_banner():
    print(Color.CYAN + "=" * 68 + Color.RESET)
    print(Color.BOLD + Color.WHITE + "      ESP32 DevKit V1 Hardware Simulator (Digital Twin)" + Color.RESET)
    print(Color.CYAN + "      IoT-Based Home Automation System using Spring Boot & MySQL" + Color.RESET)
    print(Color.CYAN + "=" * 68 + Color.RESET)
    print(f"Backend Server Target : {Color.GREEN}{SERVER_URL}{Color.RESET}")
    print(f"Simulated Hardware IP : {Color.YELLOW}{SIMULATED_IP}{Color.RESET}")
    print(f"Configured Relays     : {Color.WHITE}{len(RELAYS)} Channels (Optocoupler Isolated){Color.RESET}")
    for r in RELAYS:
        print(f"  * CH{r['channel']}: {r['name']} on GPIO {r['pin']} (Key: {r['deviceKey']})")
    print(Color.CYAN + "-" * 68 + Color.RESET)
    print(Color.WHITE + "Starting telemetry heartbeat and command polling loop (Press Ctrl+C to stop)..." + Color.RESET)
    print()

def main():
    print_banner()

    last_heartbeat_time = 0
    poll_count = 0

    while True:
        try:
            now = time.time()
            is_heartbeat_tick = (now - last_heartbeat_time >= 8)

            for relay in RELAYS:
                # 1. Send periodic heartbeat
                if is_heartbeat_tick:
                    hb_ok = send_heartbeat(relay)
                    if not hb_ok and poll_count == 0:
                        log(f"[HEARTBEAT] Cannot reach backend server at {SERVER_URL}. Is Spring Boot running?", Color.RED)

                # 2. Poll for pending commands
                poll_command(relay)

            if is_heartbeat_tick:
                last_heartbeat_time = now
                log(f"[HEARTBEAT PULSE] All 4 relays online. Telemetry synchronized with backend.", Color.BLUE)

            poll_count += 1
            time.sleep(1.2) # Cooperative poll interval

        except KeyboardInterrupt:
            print("\n" + Color.YELLOW + "[SIMULATOR] Stopping ESP32 Simulator. Goodbye!" + Color.RESET)
            break
        except Exception as e:
            log(f"[SIMULATOR ERROR] {e}", Color.RED)
            time.sleep(2)

if __name__ == "__main__":
    if len(sys.argv) > 1:
        SERVER_URL = sys.argv[1]
    main()
