package com.codexgym.gym.config;

import com.codexgym.gym.entity.Invoice;
import com.codexgym.gym.entity.Member;
import com.codexgym.gym.entity.Membership;
import com.codexgym.gym.entity.MembershipPlan;
import com.codexgym.gym.entity.enums.InvoiceStatus;
import com.codexgym.gym.entity.enums.InvoiceType;
import com.codexgym.gym.entity.enums.MemberStatus;
import com.codexgym.gym.entity.enums.MembershipStatus;
import com.codexgym.gym.repository.InvoiceRepository;
import com.codexgym.gym.repository.MemberRepository;
import com.codexgym.gym.repository.MembershipPlanRepository;
import com.codexgym.gym.repository.MembershipRepository;
import com.codexgym.gym.service.InvoiceNumberGenerator;
import com.codexgym.gym.service.MemberCodeGenerator;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DevelopmentDataInitializer {

    private final Clock clock;
    private final InvoiceNumberGenerator invoiceNumberGenerator;
    private final MemberCodeGenerator memberCodeGenerator;

    @Bean
    CommandLineRunner seedGymData(
            MembershipPlanRepository membershipPlanRepository,
            MemberRepository memberRepository,
            MembershipRepository membershipRepository,
            InvoiceRepository invoiceRepository
    ) {
        return args -> {
            if (membershipPlanRepository.count() == 0) {
                membershipPlanRepository.save(createPlan("Monthly Elite", 30, new BigDecimal("2500.00"), new BigDecimal("500.00"), "FULL_ACCESS"));
                membershipPlanRepository.save(createPlan("Quarterly Strong", 90, new BigDecimal("6500.00"), BigDecimal.ZERO, "FULL_ACCESS"));
                membershipPlanRepository.save(createPlan("Annual Pro", 365, new BigDecimal("24000.00"), BigDecimal.ZERO, "VIP_ACCESS"));
            }

            if (memberRepository.count() == 0) {
                Member member = new Member();
                member.setMemberCode(memberCodeGenerator.nextMemberCode());
                member.setFirstName("Aarav");
                member.setLastName("Sharma");
                member.setPhoneNumber("+919999999991");
                member.setWhatsappNumber("+919999999991");
                member.setEmail("aarav.sharma@example.com");
                member.setStatus(MemberStatus.ACTIVE);
                member.setNotes("Seed member for local testing");
                member = memberRepository.save(member);

                MembershipPlan plan = membershipPlanRepository.findAll().get(0);
                Membership membership = new Membership();
                membership.setMember(member);
                membership.setPlan(plan);
                membership.setStartDate(LocalDate.now(clock));
                membership.setEndDate(LocalDate.now(clock).plusDays(plan.getDurationInDays() - 1L));
                membership.setStatus(MembershipStatus.ACTIVE);
                membership.setAutoRenew(false);
                membership.setAgreedPrice(plan.getPrice());
                membership = membershipRepository.save(membership);

                Invoice invoice = new Invoice();
                invoice.setMember(member);
                invoice.setMembership(membership);
                invoice.setInvoiceNumber(invoiceNumberGenerator.nextInvoiceNumber());
                invoice.setIssueDate(LocalDate.now(clock));
                invoice.setDueDate(LocalDate.now(clock).plusDays(3));
                invoice.setAmount(plan.getPrice().add(plan.getJoiningFee()));
                invoice.setDiscountAmount(BigDecimal.ZERO);
                invoice.setTaxAmount(BigDecimal.ZERO);
                invoice.setTotalAmount(plan.getPrice().add(plan.getJoiningFee()));
                invoice.setAmountPaid(BigDecimal.ZERO);
                invoice.setBalanceDue(plan.getPrice().add(plan.getJoiningFee()));
                invoice.setStatus(InvoiceStatus.PENDING);
                invoice.setType(InvoiceType.MEMBERSHIP_SIGNUP);
                invoice.setNotes("Seed invoice for local reminder testing");
                invoiceRepository.save(invoice);
            }
        };
    }

    private MembershipPlan createPlan(String name, int durationInDays, BigDecimal price, BigDecimal joiningFee, String accessLevel) {
        MembershipPlan plan = new MembershipPlan();
        plan.setName(name);
        plan.setDescription(name + " membership plan");
        plan.setDurationInDays(durationInDays);
        plan.setPrice(price);
        plan.setJoiningFee(joiningFee);
        plan.setAccessLevel(accessLevel);
        plan.setActive(true);
        return plan;
    }
}