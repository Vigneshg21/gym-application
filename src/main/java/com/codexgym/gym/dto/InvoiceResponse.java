package com.codexgym.gym.dto;

import com.codexgym.gym.entity.enums.InvoiceStatus;
import com.codexgym.gym.entity.enums.InvoiceType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record InvoiceResponse(
        UUID id,
        String invoiceNumber,
        UUID memberId,
        String memberName,
        UUID membershipId,
        LocalDate issueDate,
        LocalDate dueDate,
        BigDecimal amount,
        BigDecimal discountAmount,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        BigDecimal amountPaid,
        BigDecimal balanceDue,
        InvoiceStatus status,
        InvoiceType type,
        String notes
) {
}

