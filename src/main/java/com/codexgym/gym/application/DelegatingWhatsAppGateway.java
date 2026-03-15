package com.codexgym.gym.application;

import com.codexgym.gym.config.NotificationProperties;
import com.codexgym.gym.service.whatsapp.LoggingWhatsAppGateway;
import com.codexgym.gym.service.whatsapp.WebhookWhatsAppGateway;
import com.codexgym.gym.service.whatsapp.WhatsAppGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class DelegatingWhatsAppGateway implements WhatsAppGateway {

    private final LoggingWhatsAppGateway loggingWhatsAppGateway;
    private final ObjectProvider<WebhookWhatsAppGateway> webhookWhatsAppGatewayProvider;
    private final NotificationProperties notificationProperties;

    @Override
    public void sendMessage(String recipient, String messageBody) {
        if (!notificationProperties.getWhatsapp().isEnabled()) {
            log.info("WhatsApp delivery disabled. Skipping message to {}", recipient);
            return;
        }

        if (!notificationProperties.getWhatsapp().isDryRun()
                && notificationProperties.getWhatsapp().getWebhookUrl() != null
                && !notificationProperties.getWhatsapp().getWebhookUrl().isBlank()) {
            WebhookWhatsAppGateway webhookWhatsAppGateway = webhookWhatsAppGatewayProvider.getIfAvailable();
            if (webhookWhatsAppGateway != null) {
                webhookWhatsAppGateway.sendMessage(recipient, messageBody);
                return;
            }
        }

        loggingWhatsAppGateway.sendMessage(recipient, messageBody);
    }
}

