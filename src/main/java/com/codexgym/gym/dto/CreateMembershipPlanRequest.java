package com.codexgym.gym.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateMembershipPlanRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 1000) String description,
        @NotNull @Positive Integer durationInDays,
        @NotNull @DecimalMin(value = "0.00") BigDecimal price,
        @NotNull @DecimalMin(value = "0.00") BigDecimal joiningFee,
        @Size(max = 60) String accessLevel,
        Boolean active
) {
}

