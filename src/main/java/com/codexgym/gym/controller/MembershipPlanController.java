package com.codexgym.gym.controller;

import com.codexgym.gym.application.MembershipPlanService;
import com.codexgym.gym.dto.CreateMembershipPlanRequest;
import com.codexgym.gym.dto.MembershipPlanResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Membership Plans", description = "Membership plan management APIs")
@RestController
@RequestMapping("/api/v1/membership-plans")
@RequiredArgsConstructor
public class MembershipPlanController {

    private final MembershipPlanService membershipPlanService;

    @Operation(summary = "Create a new membership plan", description = "Creates a new membership plan with the provided details")
    @PostMapping
    public ResponseEntity<MembershipPlanResponse> createPlan(@Valid @RequestBody CreateMembershipPlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(membershipPlanService.createPlan(request));
    }

    @Operation(summary = "List all membership plans", description = "Retrieves a list of all available membership plans")
    @GetMapping
    public List<MembershipPlanResponse> listPlans() {
        return membershipPlanService.listPlans();
    }
}
