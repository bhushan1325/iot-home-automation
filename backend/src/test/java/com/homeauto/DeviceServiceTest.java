package com.homeauto;

import com.homeauto.dto.DeviceCreateRequest;
import com.homeauto.dto.DeviceDto;
import com.homeauto.entity.Device;
import com.homeauto.entity.DeviceType;
import com.homeauto.entity.Room;
import com.homeauto.repository.DeviceEventRepository;
import com.homeauto.repository.DeviceRepository;
import com.homeauto.repository.RoomRepository;
import com.homeauto.service.DeviceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private DeviceEventRepository deviceEventRepository;

    @InjectMocks
    private DeviceService deviceService;

    private Room testRoom;

    @BeforeEach
    void setUp() {
        testRoom = new Room("Living Room", "Main Hall");
        testRoom.setId(1L);
    }

    @Test
    @DisplayName("Successfully create device with safe GPIO pin 26")
    void testCreateDeviceWithSafePin() {
        DeviceCreateRequest request = new DeviceCreateRequest();
        request.setDeviceKey("ESP32_TEST_01");
        request.setName("Test Light");
        request.setRoomId(1L);
        request.setType(DeviceType.LIGHT);
        request.setPinNumber(26); // Safe GPIO
        request.setRelayChannel(1);
        request.setDeviceSecret("sec_12345");

        when(deviceRepository.existsByDeviceKey("ESP32_TEST_01")).thenReturn(false);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> {
            Device d = invocation.getArgument(0);
            d.setId(10L);
            return d;
        });

        DeviceDto created = deviceService.createDevice(request);

        assertNotNull(created);
        assertEquals("ESP32_TEST_01", created.getDeviceKey());
        assertEquals(26, created.getPinNumber());
        assertEquals(1, created.getRelayChannel());
        verify(deviceRepository, times(1)).save(any(Device.class));
        verify(deviceEventRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Reject device creation with unsafe SPI flash GPIO pin 6")
    void testRejectUnsafeSpiFlashPin() {
        DeviceCreateRequest request = new DeviceCreateRequest();
        request.setDeviceKey("ESP32_UNSAFE");
        request.setName("Dangerous Pin");
        request.setRoomId(1L);
        request.setType(DeviceType.LIGHT);
        request.setPinNumber(6); // Prohibited SPI flash pin
        request.setRelayChannel(1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            deviceService.createDevice(request);
        });

        assertTrue(ex.getMessage().contains("Invalid or unsafe GPIO pin"));
        verify(deviceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Reject device creation with input-only GPIO pin 36")
    void testRejectInputOnlyPin() {
        DeviceCreateRequest request = new DeviceCreateRequest();
        request.setDeviceKey("ESP32_SENSOR_PIN");
        request.setName("Sensor Pin");
        request.setRoomId(1L);
        request.setType(DeviceType.LIGHT);
        request.setPinNumber(36); // Input only (VP)
        request.setRelayChannel(1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            deviceService.createDevice(request);
        });

        assertTrue(ex.getMessage().contains("Invalid or unsafe GPIO pin"));
    }

    @Test
    @DisplayName("Reject duplicate device key registration")
    void testRejectDuplicateDeviceKey() {
        DeviceCreateRequest request = new DeviceCreateRequest();
        request.setDeviceKey("ESP32_DUPLICATE");
        request.setName("Duplicate Light");
        request.setRoomId(1L);
        request.setType(DeviceType.LIGHT);
        request.setPinNumber(26);
        request.setRelayChannel(1);

        when(deviceRepository.existsByDeviceKey("ESP32_DUPLICATE")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            deviceService.createDevice(request);
        });
    }
}
