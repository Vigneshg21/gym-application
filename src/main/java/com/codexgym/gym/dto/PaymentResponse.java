package com.codexgym.gym.dto;

import com.codexgym.gym.entity.enums.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID invoiceId,
        String invoiceNumber,
        BigDecimal amount,
        LocalDateTime paymentTimestamp,
        PaymentMethod method,
        String referenceNumber,
        String collectedBy,
        String notes
) {
}

