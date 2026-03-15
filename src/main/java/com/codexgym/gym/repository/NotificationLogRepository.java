package com.codexgym.gym.repository;

import com.codexgym.gym.entity.NotificationLog;
import com.codexgym.gym.entity.enums.NotificationEventType;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {

    boolean existsByInvoiceIdAndEventTypeAndCreatedAtBetween(
            UUID invoiceId,
            NotificationEventType eventType,
            LocalDateTime start,
            LocalDateTime end
    );

    boolean existsByMembershipIdAndEventTypeAndCreatedAtBetween(
            UUID membershipId,
            NotificationEventType eventType,
            LocalDateTime start,
            LocalDateTime end
    );
}

