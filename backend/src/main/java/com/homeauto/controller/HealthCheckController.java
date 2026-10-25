package com.homeauto.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthCheckController {

    private final long startupTime = System.currentTimeMillis();

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("application", "IoT-Based Home Automation System using ESP32");
        health.put("timestamp", LocalDateTime.now().toString());
        health.put("uptimeSeconds", (System.currentTimeMillis() - startupTime) / 1000);
        health.put("components", Map.of(
                "database", "UP",
                "esp32Watchdog", "ACTIVE",
                "security", "ENABLED"
        ));
        return ResponseEntity.ok(health);
    }
}
