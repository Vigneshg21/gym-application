package com.codexgym.gym.dto;

import java.util.List;
import java.util.UUID;

public record TelegramConnectionSyncResponse(
        int updatesScanned,
        int matchedConnectRequests,
        int linkedMembers,
        List<TelegramConnectionResult> results
) {

    public record TelegramConnectionResult(
            UUID memberId,
            String memberName,
            String telegramChatId,
            String status,
            String detail
    ) {
    }
}
