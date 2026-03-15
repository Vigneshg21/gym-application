package com.codexgym.gym.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "membership_plans", indexes = {
        @Index(name = "idx_plan_active", columnList = "active")
})
@EqualsAndHashCode(callSuper = true)
public class MembershipPlan extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Integer durationInDays;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal joiningFee = BigDecimal.ZERO;

    @Column(length = 60)
    private String accessLevel;

    @Column(nullable = false)
    private Boolean active = Boolean.TRUE;
}

