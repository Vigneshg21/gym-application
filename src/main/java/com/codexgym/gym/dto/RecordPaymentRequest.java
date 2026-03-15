package com.codexgym.gym.dto;

import com.codexgym.gym.entity.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record RecordPaymentRequest(
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotNull PaymentMethod method,
        @Size(max = 80) String referenceNumber,
        @Size(max = 100) String collectedBy,
        @Size(max = 500) String notes
) {
}

