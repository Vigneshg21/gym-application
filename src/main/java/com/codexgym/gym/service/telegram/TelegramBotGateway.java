package com.codexgym.gym.service.telegram;

import com.codexgym.gym.config.NotificationProperties;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramBotGateway implements TelegramGateway {

    private final RestClient.Builder restClientBuilder;
    private final NotificationProperties notificationProperties;

    @Override
    public void sendMessage(String chatId, String messageBody) {
        NotificationProperties.TelegramProperties properties = notificationProperties.getTelegram();
        if (!properties.isEnabled()) {
            log.info("Telegram delivery disabled. Skipping message to {}", chatId);
            return;
        }

        if (properties.isDryRun()) {
            log.info("Simulated Telegram delivery to {}: {}", chatId, messageBody);
            return;
        }

        RestClient restClient = createRestClient();
        restClient.post()
                .uri("/bot{token}/sendMessage", properties.getBotToken())
                .contentType(MediaType.APPLICATION_JSON)
                .body(new TelegramSendMessageRequest(chatId, messageBody))
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void sendDocument(String chatId, String caption, String fileName, String contentType, byte[] content) {
        NotificationProperties.TelegramProperties properties = notificationProperties.getTelegram();
        if (!properties.isEnabled()) {
            log.info("Telegram delivery disabled. Skipping document {} to {}", fileName, chatId);
            return;
        }

        if (properties.isDryRun()) {
            log.info("Simulated Telegram document delivery to {}: {} ({})", chatId, fileName, caption);
            return;
        }

        RestClient restClient = createRestClient();
        restClient.post()
                .uri("/bot{token}/sendDocument", properties.getBotToken())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(buildMultipartBody(chatId, caption, fileName, contentType, content))
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public List<TelegramUpdate> getUpdates() {
        NotificationProperties.TelegramProperties properties = notificationProperties.getTelegram();
        if (!properties.isEnabled()) {
            throw new IllegalStateException("Telegram integration is disabled");
        }
        if (properties.isDryRun()) {
            throw new IllegalStateException("Telegram integration is in dry-run mode");
        }

        RestClient restClient = createRestClient();
        TelegramUpdatesResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/bot{token}/getUpdates")
                        .queryParam("limit", 100)
                        .build(properties.getBotToken()))
                .retrieve()
                .body(TelegramUpdatesResponse.class);

        if (response == null || response.result() == null) {
            return List.of();
        }
        return response.result();
    }

    private RestClient createRestClient() {
        NotificationProperties.TelegramProperties properties = notificationProperties.getTelegram();
        if (properties.getBotToken() == null || properties.getBotToken().isBlank()) {
            throw new IllegalStateException("Telegram bot token is not configured");
        }
        return restClientBuilder.baseUrl(properties.getApiBaseUrl()).build();
    }

    private MultiValueMap<String, Object> buildMultipartBody(
            String chatId,
            String caption,
            String fileName,
            String contentType,
            byte[] content
    ) {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("chat_id", chatId);
        if (caption != null && !caption.isBlank()) {
            parts.add("caption", caption);
        }
        parts.add("document", new HttpEntity<>(buildDocumentResource(fileName, content), buildDocumentHeaders(fileName, contentType)));
        return parts;
    }

    private HttpHeaders buildDocumentHeaders(String fileName, String contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("document", fileName);
        headers.setContentType(contentType == null || contentType.isBlank()
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(contentType));
        return headers;
    }

    private ByteArrayResource buildDocumentResource(String fileName, byte[] content) {
        return new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };
    }

    private record TelegramSendMessageRequest(String chat_id, String text) {
    }
}