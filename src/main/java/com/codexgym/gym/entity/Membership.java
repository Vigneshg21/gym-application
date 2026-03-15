package com.codexgym.gym.entity;

import com.codexgym.gym.entity.enums.MembershipStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "memberships", indexes = {
        @Index(name = "idx_membership_status", columnList = "status"),
        @Index(name = "idx_membership_end_date", columnList = "endDate")
})
@EqualsAndHashCode(callSuper = true)
public class Membership extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private MembershipPlan plan;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MembershipStatus status = MembershipStatus.ACTIVE;

    @Column(nullable = false)
    private Boolean autoRenew = Boolean.FALSE;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal agreedPrice;

    private LocalDateTime lastRenewedAt;

    @Column(length = 1000)
    private String notes;
}

