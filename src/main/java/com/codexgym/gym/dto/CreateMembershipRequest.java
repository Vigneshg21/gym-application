package com.codexgym.gym.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateMembershipRequest(
        @NotNull UUID memberId,
        @NotNull UUID planId,
        LocalDate startDate,
        LocalDate dueDate,
        Boolean autoRenew,
        @DecimalMin(value = "0.00") BigDecimal agreedPrice,
        @DecimalMin(value = "0.00") BigDecimal taxAmount,
        @DecimalMin(value = "0.00") BigDecimal discountAmount,
        @Size(max = 1000) String notes
) {
}

