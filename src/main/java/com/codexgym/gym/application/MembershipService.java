package com.codexgym.gym.application;

import com.codexgym.gym.application.events.MembershipCardRefreshEvent;
import com.codexgym.gym.dto.CreateMembershipRequest;
import com.codexgym.gym.dto.MembershipEnrollmentResponse;
import com.codexgym.gym.dto.MembershipResponse;
import com.codexgym.gym.dto.RenewMembershipRequest;
import com.codexgym.gym.entity.Member;
import com.codexgym.gym.entity.Membership;
import com.codexgym.gym.entity.MembershipPlan;
import com.codexgym.gym.entity.enums.InvoiceType;
import com.codexgym.gym.entity.enums.MemberStatus;
import com.codexgym.gym.entity.enums.MembershipStatus;
import com.codexgym.gym.exception.ApiException;
import com.codexgym.gym.repository.MembershipRepository;
import com.codexgym.gym.service.GymMapper;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final MemberService memberService;
    private final MembershipPlanService membershipPlanService;
    private final MembershipRepository membershipRepository;
    private final BillingService billingService;
    private final GymMapper gymMapper;
    private final Clock clock;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public MembershipEnrollmentResponse createMembership(CreateMembershipRequest request) {
        Member member = memberService.findMemberEntity(request.memberId());
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only active members can receive memberships");
        }

        MembershipPlan plan = membershipPlanService.findPlanEntity(request.planId());
        if (!Boolean.TRUE.equals(plan.getActive())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Selected membership plan is inactive");
        }

        LocalDate startDate = request.startDate() == null ? LocalDate.now(clock) : request.startDate();
        Membership membership = new Membership();
        membership.setMember(member);
        membership.setPlan(plan);
        membership.setStartDate(startDate);
        membership.setEndDate(startDate.plusDays(plan.getDurationInDays() - 1L));
        membership.setStatus(MembershipStatus.ACTIVE);
        membership.setAutoRenew(Boolean.TRUE.equals(request.autoRenew()));
        membership.setAgreedPrice(request.agreedPrice() == null ? plan.getPrice() : request.agreedPrice());
        membership.setNotes(blankToNull(request.notes()));

        Membership savedMembership = membershipRepository.save(membership);
        BigDecimal invoiceBaseAmount = savedMembership.getAgreedPrice().add(plan.getJoiningFee());
        var invoice = billingService.createInvoice(
                member,
                savedMembership,
                InvoiceType.MEMBERSHIP_SIGNUP,
                invoiceBaseAmount,
                request.taxAmount(),
                request.discountAmount(),
                startDate,
                request.dueDate(),
                "Membership signup invoice"
        );
        applicationEventPublisher.publishEvent(new MembershipCardRefreshEvent(savedMembership.getId()));

        return new MembershipEnrollmentResponse(
                gymMapper.toResponse(savedMembership),
                gymMapper.toResponse(invoice)
        );
    }

    @Transactional(readOnly = true)
    public List<MembershipResponse> listMemberships() {
        refreshExpiredMemberships();
        return membershipRepository.findAll(Sort.by(Sort.Direction.ASC, "endDate")).stream()
                .map(gymMapper::toResponse)
                .toList();
    }

    @Transactional
    public MembershipEnrollmentResponse renewMembership(UUID membershipId, RenewMembershipRequest request) {
        Membership membership = findMembershipEntity(membershipId);

        LocalDate effectiveStart = determineRenewalStartDate(membership, request.renewalDate());
        membership.setStartDate(effectiveStart);
        membership.setEndDate(effectiveStart.plusDays(membership.getPlan().getDurationInDays() - 1L));
        membership.setStatus(MembershipStatus.ACTIVE);
        membership.setAutoRenew(request.autoRenew() != null ? request.autoRenew() : membership.getAutoRenew());
        membership.setAgreedPrice(request.renewalPrice() != null ? request.renewalPrice() : membership.getAgreedPrice());
        membership.setLastRenewedAt(LocalDateTime.now(clock));
        membership.setNotes(blankToNull(request.notes()) == null ? membership.getNotes() : request.notes().trim());

        Membership savedMembership = membershipRepository.save(membership);
        var invoice = billingService.createInvoice(
                membership.getMember(),
                savedMembership,
                InvoiceType.MEMBERSHIP_RENEWAL,
                savedMembership.getAgreedPrice(),
                request.taxAmount(),
                request.discountAmount(),
                effectiveStart,
                request.dueDate(),
                "Membership renewal invoice"
        );
        applicationEventPublisher.publishEvent(new MembershipCardRefreshEvent(savedMembership.getId()));

        return new MembershipEnrollmentResponse(
                gymMapper.toResponse(savedMembership),
                gymMapper.toResponse(invoice)
        );
    }

    @Transactional
    public void refreshExpiredMemberships() {
        List<Membership> memberships = membershipRepository.findAllByStatusAndEndDateLessThanEqualOrderByEndDateAsc(
                MembershipStatus.ACTIVE,
                LocalDate.now(clock).minusDays(1)
        );
        memberships.forEach(membership -> membership.setStatus(MembershipStatus.EXPIRED));
        if (!memberships.isEmpty()) {
            membershipRepository.saveAll(memberships);
        }
    }

    @Transactional(readOnly = true)
    public List<Membership> findActiveMembershipsEndingBy(LocalDate date) {
        return membershipRepository.findAllByStatusAndEndDateLessThanEqualOrderByEndDateAsc(MembershipStatus.ACTIVE, date);
    }

    @Transactional(readOnly = true)
    public Optional<Membership> findLatestMembershipForMember(UUID memberId) {
        return membershipRepository.findFirstByMember_IdOrderByEndDateDesc(memberId);
    }

    @Transactional(readOnly = true)
    public long countMembershipsByStatus(MembershipStatus status) {
        return membershipRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public Membership findMembershipEntity(UUID membershipId) {
        return membershipRepository.findById(membershipId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Membership not found"));
    }

    private LocalDate determineRenewalStartDate(Membership membership, LocalDate requestedRenewalDate) {
        if (requestedRenewalDate != null) {
            if (membership.getStatus() == MembershipStatus.ACTIVE && !requestedRenewalDate.isAfter(membership.getEndDate())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Renewal date must be after the current membership end date");
            }
            return requestedRenewalDate;
        }

        LocalDate today = LocalDate.now(clock);
        if (membership.getEndDate().isBefore(today)) {
            return today;
        }
        return membership.getEndDate().plusDays(1);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}