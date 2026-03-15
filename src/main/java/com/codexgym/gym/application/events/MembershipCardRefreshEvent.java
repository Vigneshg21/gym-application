package com.codexgym.gym.application.events;

import java.util.UUID;

public record MembershipCardRefreshEvent(UUID membershipId) {
}