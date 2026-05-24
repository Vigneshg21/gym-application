package com.codexgym.gym.controller;

import com.codexgym.gym.application.DashboardService;
import com.codexgym.gym.dto.DashboardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dashboard", description = "Dashboard and metrics APIs")
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Get dashboard summary", description = "Retrieves dashboard metrics and summary information")
    @GetMapping
    public DashboardResponse getDashboard() {
        return dashboardService.getSummary();
    }
}
