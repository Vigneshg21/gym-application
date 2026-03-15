package com.codexgym.gym.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RenewMembershipRequest(
        LocalDate renewalDate,
        LocalDate dueDate,
        Boolean autoRenew,
        @DecimalMin(value = "0.00") BigDecimal renewalPrice,
        @DecimalMin(value = "0.00") BigDecimal taxAmount,
        @DecimalMin(value = "0.00") BigDecimal discountAmount,
        @Size(max = 1000) String notes
) {
}

