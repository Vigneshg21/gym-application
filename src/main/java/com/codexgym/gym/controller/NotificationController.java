package com.codexgym.gym.controller;

import com.codexgym.gym.application.NotificationService;
import com.codexgym.gym.dto.SendAdminTestNotificationRequest;
import com.codexgym.gym.dto.SendAnnouncementRequest;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/announcements")
    public ResponseEntity<Map<String, String>> sendAnnouncement(@Valid @RequestBody SendAnnouncementRequest request) {
        notificationService.sendAnnouncement(request.memberId(), request.message());
        return ResponseEntity.accepted().body(Map.of("status", "queued"));
    }

    @PostMapping("/admin-test")
    public ResponseEntity<Map<String, String>> sendAdminTest(
            @Valid @RequestBody SendAdminTestNotificationRequest request
    ) {
        notificationService.sendAdminTest(request.message());
        return ResponseEntity.accepted().body(Map.of("status", "queued"));
    }
}
