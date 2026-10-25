package com.homeauto.repository;

import com.homeauto.entity.Command;
import com.homeauto.entity.CommandStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommandRepository extends JpaRepository<Command, Long> {
    List<Command> findByDeviceIdAndStatusOrderByCreatedAtAsc(Long deviceId, CommandStatus status);
    List<Command> findByDeviceDeviceKeyAndStatusOrderByCreatedAtAsc(String deviceKey, CommandStatus status);
    Optional<Command> findByCommandUuid(String commandUuid);
    List<Command> findTop50ByOrderByCreatedAtDesc();
    List<Command> findByDeviceIdOrderByCreatedAtDesc(Long deviceId);
    List<Command> findByStatusAndCreatedAtBefore(CommandStatus status, LocalDateTime threshold);
}
