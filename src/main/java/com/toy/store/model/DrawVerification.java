package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 抽獎驗證實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DrawVerification {
    private Long id;
    private Long memberId;
    private String verificationCode;
    private String gameType;
    private Long gameId;
    private Long slotId;
    private String status = "PENDING"; // PENDING, VERIFIED, EXPIRED
    private LocalDateTime verifiedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt = LocalDateTime.now();
}
