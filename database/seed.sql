-- =========================================================================
-- SEED DATA: IoT-Based Home Automation System using ESP32
-- Safe development sample records
-- Password for 'admin': Admin@Home2026!
-- Password for 'demouser': User@Home2026!
-- =========================================================================

USE `home_automation`;

-- Initial Users (BCrypt 10 rounds)
INSERT INTO `users` (`id`, `username`, `email`, `password_hash`, `role`, `created_at`) VALUES
(1, 'admin', 'admin@homeauto.local', '$2a$10$tZ92rDcvl8mffWk8N9z24e0F.fS7cffM/8R2R18D809aX/z3cO71i', 'ROLE_ADMIN', NOW()),
(2, 'demouser', 'user@homeauto.local', '$2a$10$O9wR26bEfeS4hH.yO9.L8Opb2/QcR9w2mJ.pU8F8R2R18D809aX/z', 'ROLE_USER', NOW())
ON DUPLICATE KEY UPDATE `username` = VALUES(`username`);

-- Initial Rooms
INSERT INTO `rooms` (`id`, `name`, `description`, `created_at`) VALUES
(1, 'Living Room', 'Ground floor central living hall and entertainment area', NOW()),
(2, 'Master Bedroom', 'Primary bedroom with climate control and lighting circuits', NOW())
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- Initial 4 Devices
INSERT INTO `devices` (`id`, `device_key`, `name`, `room_id`, `type`, `pin_number`, `relay_channel`, `enabled`, `is_online`, `confirmed_state`, `last_requested_state`, `device_secret`, `created_at`) VALUES
(1, 'ESP32_RELAY_01', 'Living Room Light', 1, 'LIGHT', 26, 1, 1, 0, 'OFF', 'OFF', 'sec_lr_light_9921', NOW()),
(2, 'ESP32_RELAY_02', 'Bedroom Light', 2, 'LIGHT', 25, 2, 1, 0, 'OFF', 'OFF', 'sec_br_light_7732', NOW()),
(3, 'ESP32_RELAY_03', 'Living Room Fan', 1, 'FAN', 33, 3, 1, 0, 'OFF', 'OFF', 'sec_lr_fan_4418', NOW()),
(4, 'ESP32_RELAY_04', 'Bedroom Fan', 2, 'FAN', 32, 4, 1, 0, 'OFF', 'OFF', 'sec_br_fan_6654', NOW())
ON DUPLICATE KEY UPDATE `device_key` = VALUES(`device_key`);

-- Sample initial audit events
INSERT INTO `device_events` (`device_id`, `event_type`, `old_state`, `newState`, `details`, `created_at`) VALUES
(1, 'STATE_CHANGED', NULL, 'OFF', 'Initial system registration for Living Room Light', NOW()),
(2, 'STATE_CHANGED', NULL, 'OFF', 'Initial system registration for Bedroom Light', NOW()),
(3, 'STATE_CHANGED', NULL, 'OFF', 'Initial system registration for Living Room Fan', NOW()),
(4, 'STATE_CHANGED', NULL, 'OFF', 'Initial system registration for Bedroom Fan', NOW());
