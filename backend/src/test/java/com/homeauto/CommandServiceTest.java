package com.homeauto;

import com.homeauto.dto.CommandResponseDto;
import com.homeauto.entity.*;
import com.homeauto.repository.CommandRepository;
import com.homeauto.repository.DeviceEventRepository;
import com.homeauto.repository.DeviceRepository;
import com.homeauto.service.CommandService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommandServiceTest {

    @Mock
    private CommandRepository commandRepository;

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private DeviceEventRepository deviceEventRepository;

    @InjectMocks
    private CommandService commandService;

    private Device testDevice;

    @BeforeEach
    void setUp() {
        Room room = new Room("Living Room", "Main Room");
        testDevice = new Device("ESP32_DEV_1", "Test Fan", room, DeviceType.FAN, 33, 1, "sec_123");
        testDevice.setId(1L);
    }

    @Test
    @DisplayName("Submit command successfully creates PENDING command and supersedes old pending")
    void testSubmitCommand() {
        Command oldPending = new Command(testDevice, DeviceState.OFF);
        oldPending.setStatus(CommandStatus.PENDING);

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(testDevice));
        when(commandRepository.findByDeviceIdAndStatusOrderByCreatedAtAsc(1L, CommandStatus.PENDING))
                .thenReturn(List.of(oldPending));
        when(commandRepository.save(any(Command.class))).thenAnswer(i -> i.getArgument(0));

        CommandResponseDto result = commandService.submitCommand(1L, DeviceState.ON);

        assertNotNull(result);
        assertEquals(DeviceState.ON, result.getRequestedState());
        assertEquals(CommandStatus.PENDING, result.getStatus());
        assertEquals(CommandStatus.FAILED, oldPending.getStatus());
        assertTrue(oldPending.getErrorMessage().contains("Superseded"));
        verify(deviceRepository, times(1)).save(testDevice);
    }

    @Test
    @DisplayName("Submit command to disabled device throws IllegalStateException")
    void testSubmitCommandToDisabledDevice() {
        testDevice.setEnabled(false);
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(testDevice));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            commandService.submitCommand(1L, DeviceState.ON);
        });

        assertTrue(ex.getMessage().contains("disabled"));
        verify(commandRepository, never()).save(any());
    }

    @Test
    @DisplayName("Submit All-Off command iterates through all enabled devices")
    void testSubmitAllOff() {
        Device device2 = new Device("ESP32_DEV_2", "Light 2", testDevice.getRoom(), DeviceType.LIGHT, 25, 2, "sec_456");
        device2.setId(2L);

        when(deviceRepository.findByEnabledTrue()).thenReturn(List.of(testDevice, device2));
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(testDevice));
        when(deviceRepository.findById(2L)).thenReturn(Optional.of(device2));
        when(commandRepository.findByDeviceIdAndStatusOrderByCreatedAtAsc(anyLong(), eq(CommandStatus.PENDING)))
                .thenReturn(Collections.emptyList());
        when(commandRepository.save(any(Command.class))).thenAnswer(i -> i.getArgument(0));

        List<CommandResponseDto> results = commandService.submitAllOff();

        assertEquals(2, results.size());
        assertEquals(DeviceState.OFF, results.get(0).getRequestedState());
        assertEquals(DeviceState.OFF, results.get(1).getRequestedState());
    }
}
