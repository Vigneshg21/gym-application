package com.codexgym.gym.repository;

import com.codexgym.gym.entity.Member;
import com.codexgym.gym.entity.enums.MemberStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, UUID> {

    long countByStatus(MemberStatus status);

    Optional<Member> findByTelegramChatId(String telegramChatId);
}
