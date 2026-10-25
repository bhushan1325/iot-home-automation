package com.homeauto;

import com.homeauto.dto.Esp32AckRequest;
import com.homeauto.dto.Esp32HeartbeatRequest;
import com.homeauto.dto.Esp32PollResponse;
import com.homeauto.entity.*;
import com.homeauto.repository.CommandRepository;
import com.homeauto.repository.DeviceEventRepository;
import com.homeauto.repository.DeviceRepository;
import com.homeauto.repository.RoomRepository;
import com.homeauto.service.CommandService;
import com.homeauto.service.Esp32Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class Esp32WorkflowIntegrationTest {

    @Autowired
    private Esp32Service esp32Service;

    @Autowired
    private CommandService commandService;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private CommandRepository commandRepository;

    @Autowired
    private DeviceEventRepository deviceEventRepository;

    private Device testDevice;

    @BeforeEach
    void setUp() {
        Room room = roomRepository.save(new Room("Test Room", "Integration Test Hall"));
        testDevice = deviceRepository.save(new Device(
                "ESP32_INTEG_01",
                "Integration Test Light",
                room,
                DeviceType.LIGHT,
                26,
                1,
                "secret_integration_token_123"
        ));
    }

    @Test
    @DisplayName("ESP32 Heartbeat updates online status and last seen timestamp")
    void testEsp32Heartbeat() {
        Esp32HeartbeatRequest request = new Esp32HeartbeatRequest(
                "ESP32_INTEG_01",
                DeviceState.OFF,
                120L,
                -65,
                "192.168.1.150"
        );

        esp32Service.processHeartbeat(request, "secret_integration_token_123");

        Device refreshed = deviceRepository.findById(testDevice.getId()).orElseThrow();
        assertTrue(refreshed.isOnline());
        assertNotNull(refreshed.getLastSeenAt());
    }

    @Test
    @DisplayName("Reject ESP32 request with invalid device token")
    void testRejectInvalidToken() {
        Esp32HeartbeatRequest request = new Esp32HeartbeatRequest(
                "ESP32_INTEG_01",
                DeviceState.OFF,
                120L,
                -65,
                "192.168.1.150"
        );

        assertThrows(AccessDeniedException.class, () -> {
            esp32Service.processHeartbeat(request, "WRONG_TOKEN");
        });
    }

    @Test
    @DisplayName("End-to-End Vertical Slice: User queues command -> ESP32 polls -> ESP32 executes and acknowledges -> State confirmed in DB")
    void testFullCommandDispatchAndAckFlow() {
        // Step 1: User issues ON command
        var cmdResponse = commandService.submitCommand(testDevice.getId(), DeviceState.ON);
        assertNotNull(cmdResponse);
        assertEquals(CommandStatus.PENDING, cmdResponse.getStatus());

        // Step 2: ESP32 polls pending commands
        Esp32PollResponse pollRes = esp32Service.pollPendingCommand("ESP32_INTEG_01", "secret_integration_token_123");
        assertTrue(pollRes.isHasPendingCommand());
        assertEquals("ON", pollRes.getRequestedState());
        assertEquals(26, pollRes.getPinNumber());
        assertEquals(cmdResponse.getCommandUuid(), pollRes.getCommandUuid());

        // Verify status transitioned to SENT
        Command sentCmd = commandRepository.findByCommandUuid(cmdResponse.getCommandUuid()).orElseThrow();
        assertEquals(CommandStatus.SENT, sentCmd.getStatus());
        assertNotNull(sentCmd.getSentAt());

        // Step 3: ESP32 executes relay switch and acknowledges success
        Esp32AckRequest ackReq = new Esp32AckRequest(
                pollRes.getCommandUuid(),
                "ESP32_INTEG_01",
                DeviceState.ON,
                true
        );
        esp32Service.acknowledgeCommand(ackReq, "secret_integration_token_123");

        // Step 4: Verify Command is ACKNOWLEDGED and Device confirmedState is ON
        Command ackedCmd = commandRepository.findByCommandUuid(cmdResponse.getCommandUuid()).orElseThrow();
        assertEquals(CommandStatus.ACKNOWLEDGED, ackedCmd.getStatus());
        assertNotNull(ackedCmd.getAcknowledgedAt());

        Device confirmedDevice = deviceRepository.findById(testDevice.getId()).orElseThrow();
        assertEquals(DeviceState.ON, confirmedDevice.getConfirmedState());
        assertTrue(confirmedDevice.isOnline());
    }
}
