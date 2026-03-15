package com.codexgym.gym.application;

import com.codexgym.gym.application.events.MemberCreatedEvent;
import com.codexgym.gym.dto.CreateMemberRequest;
import com.codexgym.gym.dto.MemberResponse;
import com.codexgym.gym.entity.Member;
import com.codexgym.gym.exception.ApiException;
import com.codexgym.gym.repository.MemberRepository;
import com.codexgym.gym.service.GymMapper;
import com.codexgym.gym.service.MemberCodeGenerator;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberCodeGenerator memberCodeGenerator;
    private final GymMapper gymMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public MemberResponse createMember(CreateMemberRequest request) {
        try {
            Member member = new Member();
            member.setMemberCode(memberCodeGenerator.nextMemberCode());
            member.setFirstName(request.firstName().trim());
            member.setLastName(request.lastName().trim());
            member.setPhoneNumber(request.phoneNumber().trim());
            member.setWhatsappNumber(blankToNull(request.whatsappNumber()));
            member.setEmail(blankToNull(request.email()));
            member.setTelegramChatId(blankToNull(request.telegramChatId()));
            member.setDateOfBirth(request.dateOfBirth());
            member.setEmergencyContactName(blankToNull(request.emergencyContactName()));
            member.setEmergencyContactPhone(blankToNull(request.emergencyContactPhone()));
            member.setNotes(blankToNull(request.notes()));
            member.setProfileImageContentType(resolveProfileImageContentType(request));
            member.setProfileImage(decodeProfileImage(request.profileImageBase64()));

            Member savedMember = memberRepository.save(member);
            applicationEventPublisher.publishEvent(new MemberCreatedEvent(savedMember.getId()));
            return gymMapper.toResponse(savedMember);
        } catch (DataIntegrityViolationException exception) {
            throw new ApiException(HttpStatus.CONFLICT, "Member code, phone, WhatsApp number, email, or Telegram chat ID already exists");
        }
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> listMembers() {
        return memberRepository.findAll().stream()
                .map(gymMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MemberResponse getMember(UUID memberId) {
        return gymMapper.toResponse(findMemberEntity(memberId));
    }

    @Transactional(readOnly = true)
    public Member findMemberEntity(UUID memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Member not found"));
    }

    private byte[] decodeProfileImage(String base64Value) {
        if (!hasText(base64Value)) {
            return null;
        }
        try {
            return Base64.getDecoder().decode(base64Value.trim());
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Profile image is not a valid Base64 payload");
        }
    }

    private String resolveProfileImageContentType(CreateMemberRequest request) {
        String base64Value = blankToNull(request.profileImageBase64());
        String contentType = blankToNull(request.profileImageContentType());
        if (base64Value == null && contentType == null) {
            return null;
        }
        if (base64Value == null || contentType == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Profile image and content type must be provided together");
        }
        return contentType;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}