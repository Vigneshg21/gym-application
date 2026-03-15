package com.codexgym.gym.entity;

import com.codexgym.gym.entity.enums.NotificationEventType;
import com.codexgym.gym.entity.enums.NotificationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "notification_logs", indexes = {
        @Index(name = "idx_notification_status", columnList = "status"),
        @Index(name = "idx_notification_type", columnList = "eventType"),
        @Index(name = "idx_notification_created", columnList = "createdAt")
})
@EqualsAndHashCode(callSuper = true)
public class NotificationLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_id")
    private Membership membership;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationEventType eventType;

    @Column(nullable = false, length = 30)
    private String channel;

    @Column(nullable = false, length = 180)
    private String recipient;

    @Column(nullable = false, length = 1200)
    private String messageBody;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationStatus status;

    @Column(length = 1500)
    private String errorMessage;

    private LocalDateTime sentAt;
}
