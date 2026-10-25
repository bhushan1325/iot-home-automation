package com.homeauto.controller;

import com.homeauto.dto.*;
import com.homeauto.entity.Device;
import com.homeauto.entity.DeviceState;
import com.homeauto.service.CommandService;
import com.homeauto.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;
    private final CommandService commandService;

    public DeviceController(DeviceService deviceService, CommandService commandService) {
        this.deviceService = deviceService;
        this.commandService = commandService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DeviceDto>>> getAllDevices() {
        List<DeviceDto> devices = deviceService.getAllDevices();
        return ResponseEntity.ok(ApiResponse.ok("Devices retrieved successfully", devices));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeviceDto>> getDeviceById(@PathVariable Long id) {
        DeviceDto device = deviceService.getDeviceById(id);
        return ResponseEntity.ok(ApiResponse.ok("Device details retrieved", device));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DeviceDto>> createDevice(@Valid @RequestBody DeviceCreateRequest request) {
        DeviceDto created = deviceService.createDevice(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Device registered successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DeviceDto>> updateDevice(@PathVariable Long id,
                                                               @Valid @RequestBody DeviceUpdateRequest request) {
        DeviceDto updated = deviceService.updateDevice(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Device updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.ok(ApiResponse.ok("Device removed successfully"));
    }

    @PostMapping("/{id}/commands")
    public ResponseEntity<ApiResponse<CommandResponseDto>> sendCommand(
            @PathVariable Long id,
            @Valid @RequestBody CommandRequest request) {
        CommandResponseDto response = commandService.submitCommand(id, request.getRequestedState());
        return ResponseEntity.ok(ApiResponse.ok("Command queued successfully for hardware dispatch", response));
    }

    @PostMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<CommandResponseDto>> toggleDevice(@PathVariable Long id) {
        Device device = deviceService.getDeviceEntity(id);
        DeviceState targetState = (device.getConfirmedState() == DeviceState.ON) ? DeviceState.OFF : DeviceState.ON;
        CommandResponseDto response = commandService.submitCommand(id, targetState);
        return ResponseEntity.ok(ApiResponse.ok("Toggle command queued for " + targetState, response));
    }

    @GetMapping("/{id}/commands")
    public ResponseEntity<ApiResponse<List<CommandResponseDto>>> getDeviceCommands(@PathVariable Long id) {
        List<CommandResponseDto> commands = commandService.getDeviceCommands(id);
        return ResponseEntity.ok(ApiResponse.ok("Device command history retrieved", commands));
    }
}
