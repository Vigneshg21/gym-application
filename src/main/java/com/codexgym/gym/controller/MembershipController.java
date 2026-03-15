package com.codexgym.gym.controller;

import com.codexgym.gym.application.MemberCardService;
import com.codexgym.gym.application.MembershipService;
import com.codexgym.gym.application.NotificationService;
import com.codexgym.gym.dto.CreateMembershipRequest;
import com.codexgym.gym.dto.MembershipEnrollmentResponse;
import com.codexgym.gym.dto.MembershipResponse;
import com.codexgym.gym.dto.RenewMembershipRequest;
import com.codexgym.gym.messaging.NotificationAttachment;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/memberships")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;
    private final MemberCardService memberCardService;
    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<MembershipEnrollmentResponse> createMembership(
            @Valid @RequestBody CreateMembershipRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(membershipService.createMembership(request));
    }

    @GetMapping
    public List<MembershipResponse> listMemberships() {
        return membershipService.listMemberships();
    }

    @GetMapping("/{membershipId}/card-document")
    public ResponseEntity<byte[]> downloadMembershipCard(@PathVariable UUID membershipId) {
        return buildAttachmentResponse(memberCardService.prepareMembershipCard(membershipId).attachment());
    }

    @PostMapping("/{membershipId}/card-notifications")
    public ResponseEntity<Map<String, String>> sendMembershipCard(@PathVariable UUID membershipId) {
        notificationService.queueMembershipCard(membershipId);
        return ResponseEntity.accepted().body(Map.of("status", "queued"));
    }

    @PostMapping("/{membershipId}/renew")
    public MembershipEnrollmentResponse renewMembership(
            @PathVariable UUID membershipId,
            @Valid @RequestBody RenewMembershipRequest request
    ) {
        return membershipService.renewMembership(membershipId, request);
    }

    private ResponseEntity<byte[]> buildAttachmentResponse(NotificationAttachment attachment) {
        MediaType mediaType = attachment.getContentType() == null || attachment.getContentType().isBlank()
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(attachment.getContentType());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"")
                .contentType(mediaType)
                .body(attachment.getContent());
    }
}