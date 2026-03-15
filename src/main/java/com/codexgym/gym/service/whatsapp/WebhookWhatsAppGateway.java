package com.codexgym.gym.service.whatsapp;

import com.codexgym.gym.config.NotificationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "gym.notifications.whatsapp", name = "webhook-url")
public class WebhookWhatsAppGateway implements WhatsAppGateway {

    private final RestClient.Builder restClientBuilder;
    private final NotificationProperties notificationProperties;

    @Override
    public void sendMessage(String recipient, String messageBody) {
        RestClient restClient = restClientBuilder.baseUrl(notificationProperties.getWhatsapp().getWebhookUrl()).build();
        restClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> addHeaders(headers, notificationProperties))
                .body(new WhatsAppWebhookRequest(recipient, messageBody, "whatsapp"))
                .retrieve()
                .toBodilessEntity();
    }

    private void addHeaders(HttpHeaders headers, NotificationProperties properties) {
        String bearerToken = properties.getWhatsapp().getBearerToken();
        if (bearerToken != null && !bearerToken.isBlank()) {
            headers.setBearerAuth(bearerToken);
        }
    }

    private record WhatsAppWebhookRequest(String to, String message, String channel) {
    }
}

