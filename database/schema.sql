-- =========================================================================
-- DATABASE SCHEMA: IoT-Based Home Automation System using ESP32
-- Target Database: MySQL 8.x
-- Author: Senior Full-Stack & IoT Systems Engineer
-- =========================================================================

CREATE DATABASE IF NOT EXISTS `home_automation` 
  CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

USE `home_automation`;

-- Disable foreign key checks during table creation
SET FOREIGN_KEY_CHECKS = 0;

-- 1. Users Table
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL,
    `email` VARCHAR(100) NOT NULL,
    `password_hash` VARCHAR(255) NOT NULL,
    `role` VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_users_username` (`username`),
    UNIQUE KEY `uk_users_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Rooms Table
DROP TABLE IF EXISTS `rooms`;
CREATE TABLE `rooms` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `description` VARCHAR(255) NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_rooms_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Devices Table
DROP TABLE IF EXISTS `devices`;
CREATE TABLE `devices` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `device_key` VARCHAR(64) NOT NULL,
    `name` VARCHAR(100) NOT NULL,
    `room_id` BIGINT NOT NULL,
    `type` VARCHAR(20) NOT NULL DEFAULT 'LIGHT',
    `pin_number` INT NOT NULL,
    `relay_channel` INT NOT NULL,
    `enabled` TINYINT(1) NOT NULL DEFAULT 1,
    `is_online` TINYINT(1) NOT NULL DEFAULT 0,
    `last_seen_at` DATETIME NULL,
    `confirmed_state` VARCHAR(10) NOT NULL DEFAULT 'OFF',
    `last_requested_state` VARCHAR(10) NOT NULL DEFAULT 'OFF',
    `device_secret` VARCHAR(128) NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_device_key` (`device_key`),
    KEY `idx_device_room` (`room_id`),
    KEY `idx_device_state` (`confirmed_state`),
    KEY `idx_device_online` (`is_online`),
    CONSTRAINT `fk_devices_room` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Commands Table
DROP TABLE IF EXISTS `commands`;
CREATE TABLE `commands` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `command_uuid` VARCHAR(36) NOT NULL,
    `device_id` BIGINT NOT NULL,
    `requested_state` VARCHAR(10) NOT NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    `error_message` VARCHAR(500) NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `sent_at` DATETIME NULL,
    `acknowledged_at` DATETIME NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_command_uuid` (`command_uuid`),
    KEY `idx_cmd_dev_status` (`device_id`, `status`),
    KEY `idx_cmd_created` (`created_at`),
    CONSTRAINT `fk_commands_device` FOREIGN KEY (`device_id`) REFERENCES `devices` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Device Events (Audit / History Log)
DROP TABLE IF EXISTS `device_events`;
CREATE TABLE `device_events` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `device_id` BIGINT NOT NULL,
    `event_type` VARCHAR(30) NOT NULL,
    `old_state` VARCHAR(10) NULL,
    `new_state` VARCHAR(10) NULL,
    `details` VARCHAR(500) NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_event_device_time` (`device_id`, `created_at`),
    KEY `idx_event_created` (`created_at`),
    CONSTRAINT `fk_events_device` FOREIGN KEY (`device_id`) REFERENCES `devices` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
