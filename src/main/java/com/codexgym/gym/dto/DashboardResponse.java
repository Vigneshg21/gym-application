package com.codexgym.gym.dto;

import java.math.BigDecimal;

public record DashboardResponse(
        long totalMembers,
        long activeMembers,
        long activeMemberships,
        long membershipsExpiringSoon,
        long overdueInvoices,
        long pendingInvoices,
        BigDecimal revenueCollectedThisMonth,
        BigDecimal outstandingReceivables
) {
}

