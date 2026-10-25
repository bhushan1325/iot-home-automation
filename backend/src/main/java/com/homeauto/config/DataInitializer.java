package com.homeauto.config;

import com.homeauto.entity.*;
import com.homeauto.repository.DeviceRepository;
import com.homeauto.repository.RoomRepository;
import com.homeauto.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final DeviceRepository deviceRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           RoomRepository roomRepository,
                           DeviceRepository deviceRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.deviceRepository = deviceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Initialize Default Admin and Demo User if none exist
        if (userRepository.count() == 0) {
            log.info("Bootstrapping initial users...");

            User admin = new User(
                    "admin",
                    "admin@homeauto.local",
                    passwordEncoder.encode("Admin@Home2026!"),
                    Role.ROLE_ADMIN
            );
            userRepository.save(admin);

            User user = new User(
                    "demouser",
                    "user@homeauto.local",
                    passwordEncoder.encode("User@Home2026!"),
                    Role.ROLE_USER
            );
            userRepository.save(user);

            log.info("Initialized default users: 'admin' and 'demouser'");
        }

        // Initialize Default Rooms
        if (roomRepository.count() == 0) {
            log.info("Bootstrapping default rooms...");
            Room livingRoom = roomRepository.save(new Room("Living Room", "Ground floor central living area"));
            Room bedroom = roomRepository.save(new Room("Master Bedroom", "First floor master bedroom"));

            // Initialize the 4 default devices specified in project requirements
            if (deviceRepository.count() == 0) {
                log.info("Bootstrapping initial 4 home automation devices...");

                deviceRepository.save(new Device(
                        "ESP32_RELAY_01",
                        "Living Room Light",
                        livingRoom,
                        DeviceType.LIGHT,
                        26, // GPIO 26
                        1,  // Relay Channel 1
                        "sec_lr_light_9921"
                ));

                deviceRepository.save(new Device(
                        "ESP32_RELAY_02",
                        "Bedroom Light",
                        bedroom,
                        DeviceType.LIGHT,
                        25, // GPIO 25
                        2,  // Relay Channel 2
                        "sec_br_light_7732"
                ));

                deviceRepository.save(new Device(
                        "ESP32_RELAY_03",
                        "Living Room Fan",
                        livingRoom,
                        DeviceType.FAN,
                        33, // GPIO 33
                        3,  // Relay Channel 3
                        "sec_lr_fan_4418"
                ));

                deviceRepository.save(new Device(
                        "ESP32_RELAY_04",
                        "Bedroom Fan",
                        bedroom,
                        DeviceType.FAN,
                        32, // GPIO 32
                        4,  // Relay Channel 4
                        "sec_br_fan_6654"
                ));

                log.info("Bootstrapped 4 default devices with safe GPIO mappings.");
            }
        }
    }
}
