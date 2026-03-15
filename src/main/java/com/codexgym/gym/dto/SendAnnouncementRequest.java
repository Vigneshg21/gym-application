package com.codexgym.gym.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record SendAnnouncementRequest(
        @NotNull UUID memberId,
        @NotBlank @Size(max = 1000) String message
) {
}

