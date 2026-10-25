package com.homeauto.controller;

import com.homeauto.dto.ApiResponse;
import com.homeauto.dto.CommandResponseDto;
import com.homeauto.dto.DashboardSummaryDto;
import com.homeauto.dto.DeviceDto;
import com.homeauto.entity.DeviceState;
import com.homeauto.repository.DeviceRepository;
import com.homeauto.service.CommandService;
import com.homeauto.service.DeviceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class DashboardController {

    private final DeviceRepository deviceRepository;
    private final DeviceService deviceService;
    private final CommandService commandService;

    public DashboardController(DeviceRepository deviceRepository,
                               DeviceService deviceService,
                               CommandService commandService) {
        this.deviceRepository = deviceRepository;
        this.deviceService = deviceService;
        this.commandService = commandService;
    }

    @GetMapping("/api/dashboard/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryDto>> getDashboardSummary() {
        List<DeviceDto> devices = deviceService.getAllDevices();
        long total = devices.size();
        long online = devices.stream().filter(DeviceDto::isOnline).count();
        long active = devices.stream().filter(d -> d.getConfirmedState() == DeviceState.ON).count();
        long offline = total - online;

        DashboardSummaryDto summary = new DashboardSummaryDto(total, online, active, offline, devices);
        return ResponseEntity.ok(ApiResponse.ok("Dashboard summary retrieved", summary));
    }

    @PostMapping("/api/commands/all-off")
    public ResponseEntity<ApiResponse<List<CommandResponseDto>>> triggerAllOff() {
        List<CommandResponseDto> responses = commandService.submitAllOff();
        return ResponseEntity.ok(ApiResponse.ok("All-Off sequence dispatched to all active devices", responses));
    }
}
