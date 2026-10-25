package com.homeauto.repository;

import com.homeauto.entity.DeviceEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DeviceEventRepository extends JpaRepository<DeviceEvent, Long> {
    List<DeviceEvent> findTop100ByOrderByCreatedAtDesc();
    List<DeviceEvent> findByDeviceIdOrderByCreatedAtDesc(Long deviceId);
    List<DeviceEvent> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);
    List<DeviceEvent> findByDeviceIdAndCreatedAtBetweenOrderByCreatedAtDesc(Long deviceId, LocalDateTime start, LocalDateTime end);
}
