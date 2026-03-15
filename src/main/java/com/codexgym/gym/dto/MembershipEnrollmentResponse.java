package com.codexgym.gym.dto;

public record MembershipEnrollmentResponse(
        MembershipResponse membership,
        InvoiceResponse invoice
) {
}

