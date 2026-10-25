package com.homeauto.controller;

import com.homeauto.dto.ApiResponse;
import com.homeauto.dto.DeviceEventDto;
import com.homeauto.entity.DeviceEvent;
import com.homeauto.repository.DeviceEventRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final DeviceEventRepository deviceEventRepository;

    public HistoryController(DeviceEventRepository deviceEventRepository) {
        this.deviceEventRepository = deviceEventRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DeviceEventDto>>> getEventHistory(
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<DeviceEvent> events;

        if (deviceId != null && startDate != null && endDate != null) {
            events = deviceEventRepository.findByDeviceIdAndCreatedAtBetweenOrderByCreatedAtDesc(deviceId, startDate, endDate);
        } else if (deviceId != null) {
            events = deviceEventRepository.findByDeviceIdOrderByCreatedAtDesc(deviceId);
        } else if (startDate != null && endDate != null) {
            events = deviceEventRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate);
        } else {
            events = deviceEventRepository.findTop100ByOrderByCreatedAtDesc();
        }

        List<DeviceEventDto> dtos = events.stream()
                .map(DeviceEventDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok("History events retrieved", dtos));
    }
}
