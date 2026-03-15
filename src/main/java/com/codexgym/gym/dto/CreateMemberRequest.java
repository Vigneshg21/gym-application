package com.codexgym.gym.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateMemberRequest(
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @NotBlank @Pattern(regexp = "^[+]?[0-9]{8,15}$", message = "Phone number must be numeric and 8-15 digits")
        String phoneNumber,
        @Pattern(regexp = "^[+]?[0-9]{8,15}$", message = "WhatsApp number must be numeric and 8-15 digits")
        String whatsappNumber,
        @Email @Size(max = 120) String email,
        @Pattern(regexp = "^-?[0-9]{6,20}$", message = "Telegram chat ID must be numeric")
        String telegramChatId,
        LocalDate dateOfBirth,
        @Size(max = 100) String emergencyContactName,
        @Pattern(regexp = "^[+]?[0-9]{8,15}$", message = "Emergency contact phone must be numeric and 8-15 digits")
        String emergencyContactPhone,
        @Size(max = 1000) String notes,
        @Size(max = 8000000) String profileImageBase64,
        @Pattern(regexp = "^image/[a-zA-Z0-9.+-]+$", message = "Profile image must be a valid image content type")
        String profileImageContentType
) {
}