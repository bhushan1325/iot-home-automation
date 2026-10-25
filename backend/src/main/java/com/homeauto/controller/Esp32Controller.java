package com.homeauto.controller;

import com.homeauto.dto.ApiResponse;
import com.homeauto.dto.Esp32AckRequest;
import com.homeauto.dto.Esp32HeartbeatRequest;
import com.homeauto.dto.Esp32PollResponse;
import com.homeauto.service.Esp32Service;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/esp32")
public class Esp32Controller {

    private final Esp32Service esp32Service;

    public Esp32Controller(Esp32Service esp32Service) {
        this.esp32Service = esp32Service;
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<ApiResponse<String>> heartbeat(
            @RequestHeader(value = "X-Device-Token", required = false) String deviceToken,
            @Valid @RequestBody Esp32HeartbeatRequest request) {
        esp32Service.processHeartbeat(request, deviceToken);
        return ResponseEntity.ok(ApiResponse.ok("Heartbeat accepted"));
    }

    @GetMapping("/poll")
    public ResponseEntity<Esp32PollResponse> poll(
            @RequestHeader(value = "X-Device-Key", required = false) String deviceKey,
            @RequestHeader(value = "X-Device-Token", required = false) String deviceToken) {
        Esp32PollResponse response = esp32Service.pollPendingCommand(deviceKey, deviceToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/acknowledge")
    public ResponseEntity<ApiResponse<String>> acknowledge(
            @RequestHeader(value = "X-Device-Token", required = false) String deviceToken,
            @Valid @RequestBody Esp32AckRequest request) {
        esp32Service.acknowledgeCommand(request, deviceToken);
        return ResponseEntity.ok(ApiResponse.ok("Command execution acknowledged"));
    }
}
