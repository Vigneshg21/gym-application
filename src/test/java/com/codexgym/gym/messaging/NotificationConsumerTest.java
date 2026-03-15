package com.codexgym.gym.messaging;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;

import com.codexgym.gym.application.NotificationService;
import com.codexgym.gym.entity.enums.NotificationChannel;
import com.codexgym.gym.entity.enums.NotificationEventType;
import com.codexgym.gym.service.email.EmailGateway;
import com.codexgym.gym.service.telegram.TelegramGateway;
import com.codexgym.gym.service.whatsapp.WhatsAppGateway;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationConsumerTest {

    @Mock
    private WhatsAppGateway whatsAppGateway;

    @Mock
    private EmailGateway emailGateway;

    @Mock
    private TelegramGateway telegramGateway;

    @Mock
    private NotificationService notificationService;

    private NotificationConsumer notificationConsumer;

    @BeforeEach
    void setUp() {
        notificationConsumer = new NotificationConsumer(whatsAppGateway, emailGateway, telegramGateway, notificationService);
    }

    @Test
    void consumeSendsTelegramDocumentWhenAttachmentExists() {
        UUID logId = UUID.randomUUID();
        byte[] attachmentBytes = "receipt".getBytes(StandardCharsets.UTF_8);
        NotificationEventMessage message = new NotificationEventMessage(
                logId,
                UUID.randomUUID(),
                NotificationEventType.PAYMENT_RECEIPT,
                NotificationChannel.TELEGRAM,
                "member-chat",
                "Receipt",
                "Invoice paid successfully",
                LocalDateTime.of(2026, 3, 14, 10, 0),
                new NotificationAttachment("receipt.html", "text/html", attachmentBytes)
        );

        notificationConsumer.consume(message);

        verify(telegramGateway).sendDocument(
                eq("member-chat"),
                eq("Invoice paid successfully"),
                eq("receipt.html"),
                eq("text/html"),
                same(attachmentBytes)
        );
        verify(notificationService).markDelivered(logId);
    }
}