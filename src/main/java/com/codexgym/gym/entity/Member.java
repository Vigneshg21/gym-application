package com.codexgym.gym.entity;

import com.codexgym.gym.entity.enums.MemberStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "members", indexes = {
        @Index(name = "idx_member_status", columnList = "status"),
        @Index(name = "idx_member_phone", columnList = "phoneNumber"),
        @Index(name = "idx_member_telegram_chat", columnList = "telegramChatId")
})
@EqualsAndHashCode(callSuper = true)
public class Member extends BaseEntity {

    @Column(nullable = false, unique = true, length = 30)
    private String memberCode;

    @Column(nullable = false, length = 80)
    private String firstName;

    @Column(nullable = false, length = 80)
    private String lastName;

    @Column(nullable = false, unique = true, length = 20)
    private String phoneNumber;

    @Column(unique = true, length = 20)
    private String whatsappNumber;

    @Column(unique = true, length = 120)
    private String email;

    @Column(unique = true, length = 40)
    private String telegramChatId;

    private LocalDate dateOfBirth;

    @Column(length = 100)
    private String emergencyContactName;

    @Column(length = 20)
    private String emergencyContactPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MemberStatus status = MemberStatus.ACTIVE;

    @Column(length = 1000)
    private String notes;

    @Lob
    @Column(name = "profile_image")
    private byte[] profileImage;

    @Column(length = 100)
    private String profileImageContentType;

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getPreferredWhatsappNumber() {
        return whatsappNumber != null && !whatsappNumber.isBlank() ? whatsappNumber : phoneNumber;
    }
}