package com.codexgym.gym.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codexgym.gym.application.events.MembershipCardRefreshEvent;
import com.codexgym.gym.dto.CreateMembershipRequest;
import com.codexgym.gym.dto.MembershipEnrollmentResponse;
import com.codexgym.gym.entity.Invoice;
import com.codexgym.gym.entity.Member;
import com.codexgym.gym.entity.Membership;
import com.codexgym.gym.entity.MembershipPlan;
import com.codexgym.gym.entity.enums.InvoiceStatus;
import com.codexgym.gym.entity.enums.InvoiceType;
import com.codexgym.gym.entity.enums.MemberStatus;
import com.codexgym.gym.entity.enums.MembershipStatus;
import com.codexgym.gym.repository.MembershipRepository;
import com.codexgym.gym.service.GymMapper;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class MembershipServiceTest {

    @Mock
    private MemberService memberService;

    @Mock
    private MembershipPlanService membershipPlanService;

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private BillingService billingService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private MembershipService membershipService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-03-14T04:00:00Z"), ZoneId.of("Asia/Calcutta"));
        membershipService = new MembershipService(
                memberService,
                membershipPlanService,
                membershipRepository,
                billingService,
                new GymMapper(),
                clock,
                applicationEventPublisher
        );
    }

    @Test
    void createMembershipCreatesInvoiceIncludingJoiningFee() {
        UUID memberId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();

        Member member = new Member();
        member.setId(memberId);
        member.setMemberCode("MEM-20260314-AB12CD");
        member.setFirstName("Ananya");
        member.setLastName("Singh");
        member.setPhoneNumber("+918888888888");
        member.setStatus(MemberStatus.ACTIVE);

        MembershipPlan plan = new MembershipPlan();
        plan.setId(planId);
        plan.setName("Monthly Elite");
        plan.setDurationInDays(30);
        plan.setPrice(new BigDecimal("2500.00"));
        plan.setJoiningFee(new BigDecimal("500.00"));
        plan.setActive(true);

        when(memberService.findMemberEntity(memberId)).thenReturn(member);
        when(membershipPlanService.findPlanEntity(planId)).thenReturn(plan);
        when(membershipRepository.save(any(Membership.class))).thenAnswer(invocation -> {
            Membership saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });
        when(billingService.createInvoice(any(), any(), any(), any(), any(), any(), any(), any(), any())).thenAnswer(invocation -> {
            Invoice invoice = new Invoice();
            invoice.setId(UUID.randomUUID());
            invoice.setMember(member);
            invoice.setMembership(invocation.getArgument(1));
            invoice.setInvoiceNumber("INV-NEW");
            invoice.setIssueDate(invocation.getArgument(6));
            invoice.setDueDate(invocation.getArgument(7) == null ? invocation.getArgument(6) : invocation.getArgument(7));
            invoice.setAmount(invocation.getArgument(3));
            invoice.setDiscountAmount(BigDecimal.ZERO);
            invoice.setTaxAmount(BigDecimal.ZERO);
            invoice.setTotalAmount(invocation.getArgument(3));
            invoice.setAmountPaid(BigDecimal.ZERO);
            invoice.setBalanceDue(invocation.getArgument(3));
            invoice.setStatus(InvoiceStatus.PENDING);
            invoice.setType(InvoiceType.MEMBERSHIP_SIGNUP);
            return invoice;
        });

        MembershipEnrollmentResponse response = membershipService.createMembership(
                new CreateMembershipRequest(memberId, planId, LocalDate.of(2026, 3, 14), null, true, null, null, null, "New signup")
        );

        assertThat(response.membership().memberId()).isEqualTo(memberId);
        assertThat(response.membership().status()).isEqualTo(MembershipStatus.ACTIVE);
        assertThat(response.invoice().amount()).isEqualByComparingTo("3000.00");
        assertThat(response.membership().endDate()).isEqualTo(LocalDate.of(2026, 4, 12));
        verify(applicationEventPublisher).publishEvent(any(MembershipCardRefreshEvent.class));
    }
}