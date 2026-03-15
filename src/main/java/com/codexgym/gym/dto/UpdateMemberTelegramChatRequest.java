package com.codexgym.gym.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateMemberTelegramChatRequest(
        @NotBlank
        @Pattern(regexp = "^-?[0-9]{6,20}$", message = "Telegram chat ID must be numeric")
        String telegramChatId
) {
}
