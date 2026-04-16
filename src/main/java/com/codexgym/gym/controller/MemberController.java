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
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestParam;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MemberCardService memberCardService;
    private final NotificationService notificationService;
    private final TelegramConnectionService telegramConnectionService;

    @PostMapping
    public ResponseEntity<MemberResponse> createMember(@Valid @RequestBody CreateMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.createMember(request));
    }

    @PutMapping("/{memberId}")
    public MemberResponse updateMember(@PathVariable UUID memberId, @Valid @RequestBody CreateMemberRequest request) {
        return memberService.updateMember(memberId, request);
    }

    @GetMapping("/by-phone")
    public MemberResponse findByPhone(@RequestParam String phoneNumber) {
        return memberService.findMemberByPhone(phoneNumber);
    }
    
    

    @GetMapping
    public List<MemberResponse> listMembers() {
        return memberService.listMembers();
    }

    @GetMapping("/{memberId}")
    public MemberResponse getMember(@PathVariable UUID memberId) {
        return memberService.getMember(memberId);
    }

    @GetMapping("/{memberId}/card-document")
    public ResponseEntity<byte[]> downloadMemberCard(@PathVariable UUID memberId) {
        return buildAttachmentResponse(memberCardService.prepareMemberCard(memberId).attachment());
    }

    @PostMapping("/{memberId}/card-notifications")
    public ResponseEntity<Map<String, String>> sendMemberCard(@PathVariable UUID memberId) {
        notificationService.queueMemberCard(memberId);
        return ResponseEntity.accepted().body(Map.of("status", "queued"));
    }

    @PutMapping("/{memberId}/telegram-chat")
    public MemberResponse updateTelegramChat(
            @PathVariable UUID memberId,
            @Valid @RequestBody UpdateMemberTelegramChatRequest request
    ) {
        return telegramConnectionService.linkMemberToChat(memberId, request.telegramChatId());
    }

    @PostMapping("/{memberId}/telegram-connect")
    public TelegramConnectSessionResponse createTelegramConnectSession(@PathVariable UUID memberId) {
        return telegramConnectionService.createConnectSession(memberId);
    }

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