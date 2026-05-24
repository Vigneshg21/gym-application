package com.codexgym.gym.controller;

import com.codexgym.gym.application.MemberCardService;
import com.codexgym.gym.application.MemberService;
import com.codexgym.gym.application.NotificationService;
import com.codexgym.gym.application.TelegramConnectionService;
import com.codexgym.gym.dto.CreateMemberRequest;
import com.codexgym.gym.dto.MemberResponse;
import com.codexgym.gym.dto.TelegramConnectSessionResponse;
import com.codexgym.gym.dto.TelegramConnectionSyncResponse;
import com.codexgym.gym.dto.UpdateMemberTelegramChatRequest;
import com.codexgym.gym.messaging.NotificationAttachment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Members", description = "Member management APIs")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MemberCardService memberCardService;
    private final NotificationService notificationService;
    private final TelegramConnectionService telegramConnectionService;

    @Operation(summary = "Create a new member", description = "Creates a new member with the provided details")
    @PostMapping
    public ResponseEntity<MemberResponse> createMember(@Valid @RequestBody CreateMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.createMember(request));
    }

    @Operation(summary = "Update an existing member", description = "Updates member information by member ID")
    @PutMapping("/{memberId}")
    public MemberResponse updateMember(@PathVariable UUID memberId, @Valid @RequestBody CreateMemberRequest request) {
        return memberService.updateMember(memberId, request);
    }

    @Operation(summary = "Find member by phone number", description = "Retrieves member details using phone number")
    @GetMapping("/by-phone")
    public MemberResponse findByPhone(@Parameter(description = "Phone number of the member") @RequestParam String phoneNumber) {
        return memberService.findMemberByPhone(phoneNumber);
    }

    @Operation(summary = "List all members", description = "Retrieves a list of all members")
    @GetMapping
    public List<MemberResponse> listMembers() {
        return memberService.listMembers();
    }

    @Operation(summary = "Get member details", description = "Retrieves detailed information for a specific member")
    @GetMapping("/{memberId}")
    public MemberResponse getMember(@Parameter(description = "Member ID") @PathVariable UUID memberId) {
        return memberService.getMember(memberId);
    }

    @Operation(summary = "Download member card", description = "Downloads member card as PDF document")
    @GetMapping("/{memberId}/card-document")
    public ResponseEntity<byte[]> downloadMemberCard(@Parameter(description = "Member ID") @PathVariable UUID memberId) {
        return buildAttachmentResponse(memberCardService.prepareMemberCard(memberId).attachment());
    }

    @Operation(summary = "Send member card", description = "Queues member card for notification delivery")
    @PostMapping("/{memberId}/card-notifications")
    public ResponseEntity<Map<String, String>> sendMemberCard(@Parameter(description = "Member ID") @PathVariable UUID memberId) {
        notificationService.queueMemberCard(memberId);
        return ResponseEntity.accepted().body(Map.of("status", "queued"));
    }

    @Operation(summary = "Update telegram chat", description = "Links a member to their Telegram chat")
    @PutMapping("/{memberId}/telegram-chat")
    public MemberResponse updateTelegramChat(
            @Parameter(description = "Member ID") @PathVariable UUID memberId,
            @Valid @RequestBody UpdateMemberTelegramChatRequest request
    ) {
        return telegramConnectionService.linkMemberToChat(memberId, request.telegramChatId());
    }

    @Operation(summary = "Create telegram connect session", description = "Creates a session for connecting member to Telegram")
    @PostMapping("/{memberId}/telegram-connect")
    public TelegramConnectSessionResponse createTelegramConnectSession(@Parameter(description = "Member ID") @PathVariable UUID memberId) {
        return telegramConnectionService.createConnectSession(memberId);
    }

    @Operation(summary = "Sync telegram connections", description = "Synchronizes all telegram connections")
    @PostMapping("/telegram-connections/sync")
    public TelegramConnectionSyncResponse syncTelegramConnections() {
        return telegramConnectionService.syncConnections();
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