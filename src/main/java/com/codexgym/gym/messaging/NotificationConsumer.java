package com.codexgym.gym.messaging;

import com.codexgym.gym.application.NotificationService;
import com.codexgym.gym.entity.enums.NotificationChannel;
import com.codexgym.gym.service.email.EmailGateway;
import com.codexgym.gym.service.telegram.TelegramGateway;
import com.codexgym.gym.service.whatsapp.WhatsAppGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final WhatsAppGateway whatsAppGateway;
    private final EmailGateway emailGateway;
    private final TelegramGateway telegramGateway;
    private final NotificationService notificationService;

    @RabbitListener(queues = "${gym.notifications.queue}")
    public void consume(NotificationEventMessage message) {
        log.info(
                "Consuming {} notification event {} for recipient {}",
                message.getChannel(),
                message.getEventType(),
                message.getRecipient()
        );
        try {
            dispatch(message);
            notificationService.markDelivered(message.getLogId());
        } catch (Exception exception) {
            notificationService.markFailed(message.getLogId(), exception.getMessage());
            throw exception;
        }
    }

    private void dispatch(NotificationEventMessage message) {
        NotificationChannel channel = message.getChannel();
        if (channel == null) {
            throw new IllegalStateException("Notification channel is missing");
        }

        switch (channel) {
            case WHATSAPP -> whatsAppGateway.sendMessage(message.getRecipient(), message.getMessageBody());
            case EMAIL -> {
                if (hasAttachment(message)) {
                    emailGateway.sendMessage(message.getRecipient(), message.getSubject(), message.getMessageBody(), message.getAttachment());
                } else {
                    emailGateway.sendMessage(message.getRecipient(), message.getSubject(), message.getMessageBody());
                }
            }
            case TELEGRAM -> {
                if (hasAttachment(message)) {
                    telegramGateway.sendDocument(
                            message.getRecipient(),
                            message.getMessageBody(),
                            message.getAttachment().getFileName(),
                            message.getAttachment().getContentType(),
                            message.getAttachment().getContent()
                    );
                } else {
                    telegramGateway.sendMessage(message.getRecipient(), message.getMessageBody());
                }
            }
            default -> throw new IllegalStateException("Unsupported notification channel: " + channel);
        }
    }

    private boolean hasAttachment(NotificationEventMessage message) {
        return message.getAttachment() != null
                && message.getAttachment().getContent() != null
                && message.getAttachment().getContent().length > 0;
    }
}