package com.codexgym.gym.controller;

import com.codexgym.gym.application.NotificationService;
import com.codexgym.gym.dto.SendAdminTestNotificationRequest;
import com.codexgym.gym.dto.SendAnnouncementRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notifications", description = "Notification management APIs")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Send announcement", description = "Sends an announcement notification to a member")
    @PostMapping("/announcements")
    public ResponseEntity<Map<String, String>> sendAnnouncement(@Valid @RequestBody SendAnnouncementRequest request) {
        notificationService.sendAnnouncement(request.memberId(), request.message());
        return ResponseEntity.accepted().body(Map.of("status", "queued"));
    }

    @Operation(summary = "Send admin test notification", description = "Sends a test notification for admin verification")
    @PostMapping("/admin-test")
    public ResponseEntity<Map<String, String>> sendAdminTest(
            @Valid @RequestBody SendAdminTestNotificationRequest request
    ) {
        notificationService.sendAdminTest(request.message());
        return ResponseEntity.accepted().body(Map.of("status", "queued"));
    }
}
