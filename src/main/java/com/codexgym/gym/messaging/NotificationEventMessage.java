package com.codexgym.gym.messaging;

import com.codexgym.gym.entity.enums.NotificationChannel;
import com.codexgym.gym.entity.enums.NotificationEventType;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEventMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private UUID logId;
    private UUID memberId;
    private NotificationEventType eventType;
    private NotificationChannel channel;
    private String recipient;
    private String subject;
    private String messageBody;
    private LocalDateTime createdAt;
    private NotificationAttachment attachment;
}