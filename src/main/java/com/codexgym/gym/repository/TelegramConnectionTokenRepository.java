package com.codexgym.gym.repository;

import com.codexgym.gym.entity.TelegramConnectionToken;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelegramConnectionTokenRepository extends JpaRepository<TelegramConnectionToken, UUID> {

    Optional<TelegramConnectionToken> findByToken(String token);

    void deleteByMember_IdAndConsumedAtIsNull(UUID memberId);
}
