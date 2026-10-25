/**
 * =========================================================================
 * PROJECT: IoT-Based Home Automation System using ESP32
 * FIRMWARE: Production C++ Arduino Sketch for ESP32 DevKit V1
 * FEATURES: Non-blocking WiFi reconnect, authenticated REST polling, 
 *           heartbeat reporting, active-low relay driving, fail-safe boot,
 *           dual HTTP and Railway HTTPS support via WiFiClientSecure.
 * =========================================================================
 */

#include <WiFi.h>
#include <HTTPClient.h>
#include <WiFiClientSecure.h>
#include <ArduinoJson.h>

// Include local configuration (Copy config.h.example to config.h)
#if __has_include("config.h")
  #include "config.h"
#else
  #include "config.h.example"
#endif

// State Tracking
struct DeviceLocalState {
  bool isConfirmedOn;
  unsigned long lastPollTime;
  unsigned long lastHeartbeatTime;
};

DeviceLocalState relayStates[NUM_RELAYS];
unsigned long lastWifiRetry = 0;
WiFiClientSecure secureClient;

// Helper: Convert boolean state to digital pin level respecting active-low logic
int getPinLevel(bool turnOn) {
  if (RELAY_ACTIVE_LOW) {
    return turnOn ? LOW : HIGH; // Active-Low: LOW activates relay
  } else {
    return turnOn ? HIGH : LOW; // Active-High: HIGH activates relay
  }
}

// Helper: Initialize HTTP or HTTPS client automatically based on URL scheme
bool initHttpConnection(HTTPClient &http, const String &url) {
  if (url.startsWith("https://")) {
    secureClient.setInsecure(); // Allows connecting to Railway HTTPS without expiring CA certs
    return http.begin(secureClient, url);
  } else {
    return http.begin(url);
  }
}

// ---------------------------------------------------------------------------
// SETUP: Safe Boot Sequence
// ---------------------------------------------------------------------------
void setup() {
  Serial.begin(115200);
  delay(1000);
  Serial.println(F("\n======================================================="));
  Serial.println(F("  IoT Home Automation System - ESP32 Firmware v1.1.0"));
  Serial.println(F("======================================================="));

  // 1. Initialize Relays in a SAFE default state (All OFF)
  for (int i = 0; i < NUM_RELAYS; i++) {
    pinMode(RELAYS[i].pin, OUTPUT);
    digitalWrite(RELAYS[i].pin, getPinLevel(false)); // Safe OFF
    relayStates[i].isConfirmedOn = false;
    relayStates[i].lastPollTime = 0;
    relayStates[i].lastHeartbeatTime = 0;

    Serial.printf("[INIT] Relay CH%d ('%s') mapped to GPIO %d [SAFE OFF]\n",
                  RELAYS[i].channel, RELAYS[i].name, RELAYS[i].pin);
  }

  // 2. Initial Wi-Fi Connection
  initWiFi();
}

// ---------------------------------------------------------------------------
// NON-BLOCKING WIFI MANAGEMENT
// ---------------------------------------------------------------------------
void initWiFi() {
  Serial.printf("[WIFI] Connecting to SSID: %s ...\n", WIFI_SSID);
  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
}

void ensureWiFiConnected() {
  if (WiFi.status() == WL_CONNECTED) {
    return;
  }

  unsigned long currentMillis = millis();
  if (currentMillis - lastWifiRetry >= WIFI_RECONNECT_MS) {
    lastWifiRetry = currentMillis;
    Serial.println(F("[WIFI] Connection lost. Reconnecting..."));
    WiFi.disconnect();
    WiFi.reconnect();
  }
}

// ---------------------------------------------------------------------------
// TELEMETRY: Periodic Heartbeat
// ---------------------------------------------------------------------------
void sendHeartbeat(int index) {
  if (WiFi.status() != WL_CONNECTED) return;

  HTTPClient http;
  String url = String(BACKEND_BASE_URL) + "/api/esp32/heartbeat";

  if (!initHttpConnection(http, url)) {
    Serial.printf("[HEARTBEAT] Connection failed for %s\n", url.c_str());
    return;
  }

  http.addHeader("Content-Type", "application/json");
  http.addHeader("X-Device-Token", RELAYS[index].deviceToken);
  http.setTimeout(5000);

  StaticJsonDocument<256> doc;
  doc["deviceKey"]       = RELAYS[index].deviceKey;
  doc["confirmedState"]  = relayStates[index].isConfirmedOn ? "ON" : "OFF";
  doc["uptimeSeconds"]   = millis() / 1000;
  doc["wifiRssi"]        = WiFi.RSSI();
  doc["ipAddress"]       = WiFi.localIP().toString();
  doc["firmwareVersion"] = "1.1.0-railway";

  String requestBody;
  serializeJson(doc, requestBody);

  int httpCode = http.POST(requestBody);
  if (httpCode != HTTP_CODE_OK && httpCode != -1) {
    Serial.printf("[HEARTBEAT] Failed for %s. HTTP: %d\n", RELAYS[index].deviceKey, httpCode);
  }
  http.end();
}

// ---------------------------------------------------------------------------
// COMMAND POLLING & EXECUTION
// ---------------------------------------------------------------------------
void pollPendingCommands(int index) {
  if (WiFi.status() != WL_CONNECTED) return;

  HTTPClient http;
  String url = String(BACKEND_BASE_URL) + "/api/esp32/poll";

  if (!initHttpConnection(http, url)) {
    return;
  }

  http.addHeader("X-Device-Key", RELAYS[index].deviceKey);
  http.addHeader("X-Device-Token", RELAYS[index].deviceToken);
  http.setTimeout(4000);

  int httpCode = http.GET();
  if (httpCode == HTTP_CODE_OK) {
    String payload = http.getString();
    StaticJsonDocument<384> doc;
    DeserializationError error = deserializeJson(doc, payload);

    if (!error && doc["hasPendingCommand"].as<bool>()) {
      const char* commandUuid = doc["commandUuid"];
      const char* requestedStateStr = doc["requestedState"];
      int targetPin = doc["pinNumber"];

      Serial.printf("[COMMAND] Received command for %s: %s (UUID: %s, Pin: %d)\n",
                    RELAYS[index].name, requestedStateStr, commandUuid, targetPin);

      // Verify targeted pin matches configured hardware relay pin
      if (targetPin == RELAYS[index].pin) {
        bool newState = (strcmp(requestedStateStr, "ON") == 0);
        
        // Execute physical hardware switch
        digitalWrite(RELAYS[index].pin, getPinLevel(newState));
        relayStates[index].isConfirmedOn = newState;

        Serial.printf("[HARDWARE] Switched GPIO %d -> %s\n", targetPin, newState ? "ON" : "OFF");

        // Send acknowledgement back to backend
        acknowledgeCommand(index, commandUuid, newState, true, "");
      } else {
        Serial.printf("[SECURITY] Pin mismatch! Configured: %d, Received: %d\n",
                      RELAYS[index].pin, targetPin);
        acknowledgeCommand(index, commandUuid, relayStates[index].isConfirmedOn, false, "Pin mismatch rejected");
      }
    }
  } else if (httpCode != HTTP_CODE_OK && httpCode != -1) {
    Serial.printf("[POLL] Failed for %s. HTTP: %d\n", RELAYS[index].deviceKey, httpCode);
  }
  http.end();
}

// ---------------------------------------------------------------------------
// COMMAND ACKNOWLEDGEMENT
// ---------------------------------------------------------------------------
void acknowledgeCommand(int index, const char* uuid, bool state, bool success, const char* errorMsg) {
  HTTPClient http;
  String url = String(BACKEND_BASE_URL) + "/api/esp32/acknowledge";

  if (!initHttpConnection(http, url)) {
    return;
  }

  http.addHeader("Content-Type", "application/json");
  http.addHeader("X-Device-Token", RELAYS[index].deviceToken);
  http.setTimeout(4000);

  StaticJsonDocument<256> doc;
  doc["commandUuid"]   = uuid;
  doc["deviceKey"]     = RELAYS[index].deviceKey;
  doc["executedState"] = state ? "ON" : "OFF";
  doc["success"]       = success;
  if (strlen(errorMsg) > 0) {
    doc["errorMessage"] = errorMsg;
  }

  String body;
  serializeJson(doc, body);

  int httpCode = http.POST(body);
  if (httpCode == HTTP_CODE_OK) {
    Serial.printf("[ACK] Successfully acknowledged command UUID: %s\n", uuid);
  } else {
    Serial.printf("[ACK] Failed to send ack. HTTP: %d\n", httpCode);
  }
  http.end();
}

// ---------------------------------------------------------------------------
// MAIN LOOP: Round-robin non-blocking tasks
// ---------------------------------------------------------------------------
void loop() {
  ensureWiFiConnected();

  if (WiFi.status() == WL_CONNECTED) {
    unsigned long now = millis();

    for (int i = 0; i < NUM_RELAYS; i++) {
      // 1. Poll for pending commands
      if (now - relayStates[i].lastPollTime >= POLL_INTERVAL_MS) {
        relayStates[i].lastPollTime = now;
        pollPendingCommands(i);
      }

      // 2. Send heartbeat
      if (now - relayStates[i].lastHeartbeatTime >= HEARTBEAT_INTERVAL_MS) {
        relayStates[i].lastHeartbeatTime = now;
        sendHeartbeat(i);
      }
    }
  }

  // Small cooperative delay to yield to ESP32 RTOS background Wi-Fi tasks
  delay(50);
}
