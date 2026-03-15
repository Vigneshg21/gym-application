package com.codexgym.gym.service.whatsapp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "gym.notifications.whatsapp", name = "webhook-url", matchIfMissing = true)
public class LoggingWhatsAppGateway implements WhatsAppGateway {

    @Override
    public void sendMessage(String recipient, String messageBody) {
        log.info("Simulated WhatsApp delivery to {}: {}", recipient, messageBody);
    }
}

