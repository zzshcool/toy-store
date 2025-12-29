package com.toy.store.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "members")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    private String nickname;

    private String phone;

    // Platform Wallet Balance used for transactions
    @Column(nullable = false)
    private BigDecimal platformWalletBalance = BigDecimal.ZERO;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    private boolean enabled = true;

    private LocalDateTime lastLoginTime;

    @ManyToOne
    @JoinColumn(name = "member_level_id")
    private MemberLevel level;

    @Column(nullable = false)
    private BigDecimal monthlyRecharge = BigDecimal.ZERO;

    private String realName;

    private String address;

    private String gender;

    private java.time.LocalDate birthday;

    @Column(nullable = false)
    private Long growthValue = 0L;

    @Column(nullable = false)
    private Integer points = 0; // 積分 (原 Shards)

    @Column(nullable = false)
    private Integer bonusPoints = 0; // 紅利點數

    @Column(nullable = false)
    private Integer luckyValue = 0; // 幸運值

    private LocalDate lastLevelReviewDate;

    public enum Role {
        USER, ADMIN
    }
}
