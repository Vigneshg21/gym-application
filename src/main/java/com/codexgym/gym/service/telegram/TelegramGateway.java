package com.codexgym.gym.service.telegram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public interface TelegramGateway {

    void sendMessage(String chatId, String messageBody);

    default void sendDocument(String chatId, String caption, String fileName, String contentType, byte[] content) {
        sendMessage(chatId, caption);
    }

    List<TelegramUpdate> getUpdates();

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TelegramUpdatesResponse(boolean ok, List<TelegramUpdate> result) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TelegramUpdate(
            @JsonProperty("update_id") Long updateId,
            TelegramMessage message
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TelegramMessage(
            @JsonProperty("message_id") Long messageId,
            TelegramChat chat,
            String text
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TelegramChat(
            Long id,
            @JsonProperty("first_name") String firstName,
            String type
    ) {
    }
}