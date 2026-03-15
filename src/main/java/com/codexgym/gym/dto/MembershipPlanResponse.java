package com.codexgym.gym.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record MembershipPlanResponse(
        UUID id,
        String name,
        String description,
        Integer durationInDays,
        BigDecimal price,
        BigDecimal joiningFee,
        String accessLevel,
        boolean active
) {
}

