package com.codexgym.gym.application;

import com.codexgym.gym.config.NotificationProperties;
import com.codexgym.gym.dto.DashboardResponse;
import com.codexgym.gym.entity.enums.MemberStatus;
import com.codexgym.gym.entity.enums.MembershipStatus;
import com.codexgym.gym.repository.MemberRepository;
import com.codexgym.gym.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final MemberRepository memberRepository;
    private final MembershipService membershipService;
    private final BillingService billingService;
    private final PaymentRepository paymentRepository;
    private final NotificationProperties notificationProperties;
    private final Clock clock;

    @Transactional
    public DashboardResponse getSummary() {
        membershipService.refreshExpiredMemberships();
        billingService.refreshOverdueInvoices();

        LocalDateTime startOfMonth = LocalDate.now(clock).withDayOfMonth(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now(clock);
        BigDecimal revenue = paymentRepository.sumPaymentsBetween(startOfMonth, now);

        long expiringSoon = membershipService.findActiveMembershipsEndingBy(
                LocalDate.now(clock).plusDays(notificationProperties.getRenewalReminderWindowDays())
        ).stream().filter(membership -> !membership.getEndDate().isBefore(LocalDate.now(clock))).count();

        return new DashboardResponse(
                memberRepository.count(),
                memberRepository.countByStatus(MemberStatus.ACTIVE),
                membershipService.countMembershipsByStatus(MembershipStatus.ACTIVE),
                expiringSoon,
                billingService.countOverdueInvoices(),
                billingService.countPendingInvoices(),
                revenue == null ? BigDecimal.ZERO : revenue,
                billingService.outstandingReceivables()
        );
    }
}

