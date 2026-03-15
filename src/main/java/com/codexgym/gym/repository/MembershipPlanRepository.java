package com.codexgym.gym.repository;

import com.codexgym.gym.entity.MembershipPlan;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipPlanRepository extends JpaRepository<MembershipPlan, UUID> {

    List<MembershipPlan> findAllByActiveTrueOrderByNameAsc();
}

