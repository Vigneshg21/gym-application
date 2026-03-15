package com.codexgym.gym.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TelegramConnectSessionResponse(
        UUID memberId,
        String memberName,
        String connectToken,
        String deepLink,
        LocalDateTime expiresAt,
        String telegramChatId
) {
}
