package com.codexgym.gym.application;

import com.codexgym.gym.dto.CreateMembershipPlanRequest;
import com.codexgym.gym.dto.MembershipPlanResponse;
import com.codexgym.gym.entity.MembershipPlan;
import com.codexgym.gym.exception.ApiException;
import com.codexgym.gym.repository.MembershipPlanRepository;
import com.codexgym.gym.service.GymMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MembershipPlanService {

    private final MembershipPlanRepository membershipPlanRepository;
    private final GymMapper gymMapper;

    @Transactional
    public MembershipPlanResponse createPlan(CreateMembershipPlanRequest request) {
        MembershipPlan plan = new MembershipPlan();
        plan.setName(request.name().trim());
        plan.setDescription(blankToNull(request.description()));
        plan.setDurationInDays(request.durationInDays());
        plan.setPrice(safeMoney(request.price()));
        plan.setJoiningFee(safeMoney(request.joiningFee()));
        plan.setAccessLevel(blankToNull(request.accessLevel()));
        plan.setActive(request.active() == null || request.active());
        return gymMapper.toResponse(membershipPlanRepository.save(plan));
    }

    @Transactional(readOnly = true)
    public List<MembershipPlanResponse> listPlans() {
        return membershipPlanRepository.findAll().stream()
                .map(gymMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MembershipPlan findPlanEntity(UUID planId) {
        return membershipPlanRepository.findById(planId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Membership plan not found"));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private BigDecimal safeMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}

