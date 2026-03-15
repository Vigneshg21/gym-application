package com.codexgym.gym.service;

import com.codexgym.gym.config.NotificationProperties;
import com.codexgym.gym.entity.Invoice;
import com.codexgym.gym.entity.Member;
import com.codexgym.gym.entity.Membership;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationMessageFactory {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM uuuu", Locale.ENGLISH);

    private final NotificationProperties notificationProperties;

    public String buildInvoiceReminder(Member member, Invoice invoice) {
        return "Hi %s, this is %s. Your membership invoice %s is due on %s. Outstanding amount: %s. "
                .formatted(
                        member.getFirstName(),
                        notificationProperties.getBrandName(),
                        invoice.getInvoiceNumber(),
                        invoice.getDueDate().format(DATE_FORMATTER),
                        invoice.getBalanceDue()
                );
    }

    public String buildOverdueReminder(Member member, Invoice invoice) {
        return "Hi %s, your invoice %s is overdue since %s. Pending amount: %s. Please clear it to avoid membership disruption."
                .formatted(
                        member.getFirstName(),
                        invoice.getInvoiceNumber(),
                        invoice.getDueDate().format(DATE_FORMATTER),
                        invoice.getBalanceDue()
                );
    }

    public String buildPaymentReceipt(Member member, Invoice invoice) {
        return "Hi %s, we have marked invoice %s as fully paid. Total billed: %s. Outstanding balance: %s. Thank you for choosing %s."
                .formatted(
                        member.getFirstName(),
                        invoice.getInvoiceNumber(),
                        invoice.getTotalAmount(),
                        invoice.getBalanceDue(),
                        notificationProperties.getBrandName()
                );
    }

    public String buildMemberCardMessage(Member member, Membership membership) {
        if (membership == null) {
            return "Hi %s, welcome to %s. Your member ID card is attached. Joining date: %s."
                    .formatted(
                            member.getFirstName(),
                            notificationProperties.getBrandName(),
                            member.getCreatedAt() == null ? "today" : member.getCreatedAt().toLocalDate().format(DATE_FORMATTER)
                    );
        }

        return "Hi %s, your membership card has been updated. Plan: %s. Valid until %s. Your latest card is attached."
                .formatted(
                        member.getFirstName(),
                        membership.getPlan().getName(),
                        membership.getEndDate().format(DATE_FORMATTER)
                );
    }

    public String buildRenewalReminder(Member member, Membership membership) {
        return "Hi %s, your %s membership will expire on %s. Renewal amount: %s. Reply or visit reception to renew on time."
                .formatted(
                        member.getFirstName(),
                        membership.getPlan().getName(),
                        membership.getEndDate().format(DATE_FORMATTER),
                        membership.getAgreedPrice()
                );
    }
}