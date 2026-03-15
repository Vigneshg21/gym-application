package com.codexgym.gym.application;

import com.codexgym.gym.entity.Member;
import com.codexgym.gym.entity.Membership;
import com.codexgym.gym.messaging.NotificationAttachment;
import com.codexgym.gym.service.MemberCardDocumentFactory;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberCardService {

    private final MemberService memberService;
    private final MembershipService membershipService;
    private final MemberCardDocumentFactory memberCardDocumentFactory;

    @Transactional(readOnly = true)
    public MemberCardSnapshot prepareMemberCard(UUID memberId) {
        Member member = memberService.findMemberEntity(memberId);
        Membership membership = membershipService.findLatestMembershipForMember(memberId).orElse(null);
        return new MemberCardSnapshot(member, membership, memberCardDocumentFactory.buildMemberCard(member, membership));
    }

    @Transactional(readOnly = true)
    public MemberCardSnapshot prepareMembershipCard(UUID membershipId) {
        Membership membership = membershipService.findMembershipEntity(membershipId);
        Member member = membership.getMember();
        return new MemberCardSnapshot(member, membership, memberCardDocumentFactory.buildMemberCard(member, membership));
    }

    public record MemberCardSnapshot(Member member, Membership membership, NotificationAttachment attachment) {
    }
}