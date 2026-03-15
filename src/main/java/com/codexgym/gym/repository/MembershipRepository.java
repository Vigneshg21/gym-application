package com.codexgym.gym.repository;

import com.codexgym.gym.entity.Membership;
import com.codexgym.gym.entity.enums.MembershipStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRepository extends JpaRepository<Membership, UUID> {

    List<Membership> findAllByStatusOrderByEndDateAsc(MembershipStatus status);

    List<Membership> findAllByStatusAndEndDateLessThanEqualOrderByEndDateAsc(
            MembershipStatus status,
            LocalDate endDate
    );

    Optional<Membership> findFirstByMember_IdOrderByEndDateDesc(UUID memberId);

    long countByStatus(MembershipStatus status);
}