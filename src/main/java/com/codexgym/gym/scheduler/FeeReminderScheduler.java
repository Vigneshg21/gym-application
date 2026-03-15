package com.codexgym.gym.scheduler;

import com.codexgym.gym.application.BillingService;
import com.codexgym.gym.application.MembershipService;
import com.codexgym.gym.application.NotificationService;
import com.codexgym.gym.config.NotificationProperties;
import com.codexgym.gym.entity.Invoice;
import com.codexgym.gym.entity.Membership;
import com.codexgym.gym.entity.enums.NotificationEventType;
import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeeReminderScheduler {

    private final BillingService billingService;
    private final MembershipService membershipService;
    private final NotificationService notificationService;
    private final NotificationProperties notificationProperties;
    private final Clock clock;

    @Scheduled(cron = "${gym.notifications.scheduler-cron}")
    public void pushScheduledReminders() {
        membershipService.refreshExpiredMemberships();
        billingService.refreshOverdueInvoices();

        LocalDate today = LocalDate.now(clock);
        LocalDate invoiceThreshold = today.plusDays(notificationProperties.getFeeReminderWindowDays());
        LocalDate membershipThreshold = today.plusDays(notificationProperties.getRenewalReminderWindowDays());

        for (Invoice invoice : billingService.findInvoicesDueOnOrBefore(invoiceThreshold)) {
            NotificationEventType eventType = invoice.getDueDate().isBefore(today)
                    ? NotificationEventType.FEE_OVERDUE
                    : NotificationEventType.FEE_REMINDER;

            if (!notificationService.wasInvoiceReminderSentToday(invoice.getId(), eventType)) {
                try {
                    notificationService.queueInvoiceReminder(invoice.getId(), eventType);
                } catch (Exception exception) {
                    log.warn("Skipping invoice reminder for {} because {}", invoice.getId(), exception.getMessage());
                }
            }
        }

        for (Membership membership : membershipService.findActiveMembershipsEndingBy(membershipThreshold)) {
            if (!membership.getEndDate().isBefore(today)
                    && !notificationService.wasMembershipReminderSentToday(
                    membership.getId(),
                    NotificationEventType.MEMBERSHIP_RENEWAL_REMINDER
            )) {
                try {
                    notificationService.queueRenewalReminder(membership.getId());
                } catch (Exception exception) {
                    log.warn("Skipping renewal reminder for {} because {}", membership.getId(), exception.getMessage());
                }
            }
        }

        log.info("Gym reminder scheduler completed for {}", today);
    }
}

