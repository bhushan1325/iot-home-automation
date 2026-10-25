package com.homeauto.service;

import com.homeauto.dto.DeviceCreateRequest;
import com.homeauto.dto.DeviceDto;
import com.homeauto.dto.DeviceUpdateRequest;
import com.homeauto.entity.*;
import com.homeauto.repository.DeviceEventRepository;
import com.homeauto.repository.DeviceRepository;
import com.homeauto.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DeviceService {

    // Strictly permitted general-purpose output GPIOs on ESP32 DevKit V1
    // Excludes SPI Flash pins (6-11), input-only pins (34, 35, 36, 39), and boot strapping pins (0, 2)
    public static final Set<Integer> SAFE_OUTPUT_GPIOS = Set.of(
            4, 5, 12, 13, 14, 15, 16, 17, 18, 19, 21, 22, 23, 25, 26, 27, 32, 33
    );

    private final DeviceRepository deviceRepository;
    private final RoomRepository roomRepository;
    private final DeviceEventRepository deviceEventRepository;

    public DeviceService(DeviceRepository deviceRepository,
                         RoomRepository roomRepository,
                         DeviceEventRepository deviceEventRepository) {
        this.deviceRepository = deviceRepository;
        this.roomRepository = roomRepository;
        this.deviceEventRepository = deviceEventRepository;
    }

    public List<DeviceDto> getAllDevices() {
        return deviceRepository.findAllByOrderByRoomNameAscNameAsc()
                .stream()
                .map(DeviceDto::fromEntity)
                .collect(Collectors.toList());
    }

    public DeviceDto getDeviceById(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Device not found with id: " + id));
        return DeviceDto.fromEntity(device);
    }

    public Device getDeviceEntity(Long id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Device not found with id: " + id));
    }

    public Device getDeviceEntityByKey(String deviceKey) {
        return deviceRepository.findByDeviceKey(deviceKey)
                .orElseThrow(() -> new IllegalArgumentException("Device not found with key: " + deviceKey));
    }

    @Transactional
    public DeviceDto createDevice(DeviceCreateRequest request) {
        if (deviceRepository.existsByDeviceKey(request.getDeviceKey())) {
            throw new IllegalArgumentException("Device key already exists: " + request.getDeviceKey());
        }

        validateGpioPin(request.getPinNumber());

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Room not found with id: " + request.getRoomId()));

        String secret = request.getDeviceSecret();
        if (secret == null || secret.trim().isEmpty()) {
            secret = UUID.randomUUID().toString().replace("-", "");
        }

        Device device = new Device(
                request.getDeviceKey().trim(),
                request.getName().trim(),
                room,
                request.getType(),
                request.getPinNumber(),
                request.getRelayChannel(),
                secret
        );

        Device saved = deviceRepository.save(device);

        deviceEventRepository.save(new DeviceEvent(
                saved,
                EventType.STATE_CHANGED,
                null,
                DeviceState.OFF,
                "Device registered in system with GPIO " + saved.getPinNumber() + ", Relay CH " + saved.getRelayChannel()
        ));

        return DeviceDto.fromEntity(saved);
    }

    @Transactional
    public DeviceDto updateDevice(Long id, DeviceUpdateRequest request) {
        Device device = getDeviceEntity(id);

        validateGpioPin(request.getPinNumber());

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Room not found with id: " + request.getRoomId()));

        device.setName(request.getName().trim());
        device.setRoom(room);
        device.setType(request.getType());
        device.setPinNumber(request.getPinNumber());
        device.setRelayChannel(request.getRelayChannel());
        if (request.getEnabled() != null) {
            device.setEnabled(request.getEnabled());
        }
        device.setUpdatedAt(LocalDateTime.now());

        Device updated = deviceRepository.save(device);
        return DeviceDto.fromEntity(updated);
    }

    @Transactional
    public void deleteDevice(Long id) {
        Device device = getDeviceEntity(id);
        deviceRepository.delete(device);
    }

    public void validateGpioPin(Integer pinNumber) {
        if (pinNumber == null || !SAFE_OUTPUT_GPIOS.contains(pinNumber)) {
            throw new IllegalArgumentException("Invalid or unsafe GPIO pin: " + pinNumber +
                    ". Permitted safe ESP32 output pins are: " + SAFE_OUTPUT_GPIOS);
        }
    }
}
