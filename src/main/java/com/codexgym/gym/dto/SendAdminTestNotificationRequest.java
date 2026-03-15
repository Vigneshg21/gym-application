package com.codexgym.gym.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendAdminTestNotificationRequest(
        @NotBlank @Size(max = 1000) String message
) {
}
