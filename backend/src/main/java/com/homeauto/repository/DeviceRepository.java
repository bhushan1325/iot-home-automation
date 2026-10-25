package com.homeauto.repository;

import com.homeauto.entity.Device;
import com.homeauto.entity.DeviceState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByDeviceKey(String deviceKey);
    boolean existsByDeviceKey(String deviceKey);
    List<Device> findAllByOrderByRoomNameAscNameAsc();
    List<Device> findByEnabledTrue();
    long countByConfirmedState(DeviceState confirmedState);
    long countByIsOnline(boolean isOnline);
    List<Device> findByIsOnlineTrueAndLastSeenAtBefore(LocalDateTime threshold);
}
