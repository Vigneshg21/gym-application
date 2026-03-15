package com.codexgym.gym.service;

import com.codexgym.gym.dto.InvoiceResponse;
import com.codexgym.gym.dto.MemberResponse;
import com.codexgym.gym.dto.MembershipPlanResponse;
import com.codexgym.gym.dto.MembershipResponse;
import com.codexgym.gym.dto.PaymentResponse;
import com.codexgym.gym.entity.Invoice;
import com.codexgym.gym.entity.Member;
import com.codexgym.gym.entity.Membership;
import com.codexgym.gym.entity.MembershipPlan;
import com.codexgym.gym.entity.Payment;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class GymMapper {

    public MemberResponse toResponse(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getMemberCode(),
                member.getFirstName(),
                member.getLastName(),
                member.getFullName(),
                member.getPhoneNumber(),
                member.getPreferredWhatsappNumber(),
                member.getEmail(),
                member.getTelegramChatId(),
                toDataUrl(member.getProfileImageContentType(), member.getProfileImage()),
                member.getDateOfBirth(),
                member.getStatus(),
                member.getCreatedAt()
        );
    }

    public MembershipPlanResponse toResponse(MembershipPlan plan) {
        return new MembershipPlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getDescription(),
                plan.getDurationInDays(),
                plan.getPrice(),
                plan.getJoiningFee(),
                plan.getAccessLevel(),
                Boolean.TRUE.equals(plan.getActive())
        );
    }

    public MembershipResponse toResponse(Membership membership) {
        return new MembershipResponse(
                membership.getId(),
                membership.getMember().getId(),
                membership.getMember().getFullName(),
                membership.getPlan().getId(),
                membership.getPlan().getName(),
                membership.getStartDate(),
                membership.getEndDate(),
                membership.getStatus(),
                Boolean.TRUE.equals(membership.getAutoRenew()),
                membership.getAgreedPrice(),
                membership.getLastRenewedAt(),
                membership.getNotes()
        );
    }

    public InvoiceResponse toResponse(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getMember().getId(),
                invoice.getMember().getFullName(),
                invoice.getMembership() == null ? null : invoice.getMembership().getId(),
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getAmount(),
                invoice.getDiscountAmount(),
                invoice.getTaxAmount(),
                invoice.getTotalAmount(),
                invoice.getAmountPaid(),
                invoice.getBalanceDue(),
                invoice.getStatus(),
                invoice.getType(),
                invoice.getNotes()
        );
    }

    public PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getInvoice().getId(),
                payment.getInvoice().getInvoiceNumber(),
                payment.getAmount(),
                payment.getPaymentTimestamp(),
                payment.getMethod(),
                payment.getReferenceNumber(),
                payment.getCollectedBy(),
                payment.getNotes()
        );
    }

    private String toDataUrl(String contentType, byte[] content) {
        if (content == null || content.length == 0) {
            return null;
        }
        String safeContentType = contentType == null || contentType.isBlank() ? "image/jpeg" : contentType;
        return "data:" + safeContentType + ";base64," + Base64.getEncoder().encodeToString(content);
    }
}