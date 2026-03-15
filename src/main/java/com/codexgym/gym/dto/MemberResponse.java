package com.codexgym.gym.dto;

import com.codexgym.gym.entity.enums.MemberStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record MemberResponse(
        UUID id,
        String memberCode,
        String firstName,
        String lastName,
        String fullName,
        String phoneNumber,
        String whatsappNumber,
        String email,
        String telegramChatId,
        String profileImageDataUrl,
        LocalDate dateOfBirth,
        MemberStatus status,
        LocalDateTime createdAt
) {
}