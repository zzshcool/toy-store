package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * 會員實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    private Long id;

    private String username;

    private String password;

    private String email;

    private Role role = Role.USER;

    private String avatarUrl;

    private String nickname;

    private String phone;

    // 平台錢包餘額
    private BigDecimal platformWalletBalance = BigDecimal.ZERO;

    private LocalDateTime createdAt = LocalDateTime.now();

    private boolean enabled = true;

    private LocalDateTime lastLoginTime;

    // 會員等級 ID（手動關聯）
    private Long memberLevelId;

    // 會員等級物件（非持久化，由 Service 層填充）
    private transient MemberLevel level;

    private BigDecimal monthlyRecharge = BigDecimal.ZERO;

    private String realName;

    private String address;

    private String gender;

    private java.time.LocalDate birthday;

    private Long growthValue = 0L;

    private Integer points = 0; // 積分 (原 Shards)

    private Integer bonusPoints = 0; // 紅利點數

    private Integer luckyValue = 0; // 幸運值

    private LocalDate lastLevelReviewDate;

    public enum Role {
        USER, ADMIN
    }
}
