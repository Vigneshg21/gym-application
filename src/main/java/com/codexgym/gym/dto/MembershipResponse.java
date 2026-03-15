package com.codexgym.gym.dto;

import com.codexgym.gym.entity.enums.MembershipStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record MembershipResponse(
        UUID id,
        UUID memberId,
        String memberName,
        UUID planId,
        String planName,
        LocalDate startDate,
        LocalDate endDate,
        MembershipStatus status,
        boolean autoRenew,
        BigDecimal agreedPrice,
        LocalDateTime lastRenewedAt,
        String notes
) {
}

