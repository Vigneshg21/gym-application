package com.codexgym.gym.application;

import com.codexgym.gym.application.events.MemberCreatedEvent;
import com.codexgym.gym.application.events.MembershipCardRefreshEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberCardAutomationListener {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMemberCreated(MemberCreatedEvent event) {
        try {
            notificationService.queueMemberCard(event.memberId());
        } catch (Exception exception) {
            log.warn("Skipping automatic member card delivery for member {} because {}", event.memberId(), exception.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMembershipCardRefresh(MembershipCardRefreshEvent event) {
        try {
            notificationService.queueMembershipCard(event.membershipId());
        } catch (Exception exception) {
            log.warn("Skipping automatic membership card delivery for membership {} because {}", event.membershipId(), exception.getMessage());
        }
    }
}