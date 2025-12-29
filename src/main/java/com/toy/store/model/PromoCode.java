package com.toy.store.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 推薦碼/禮包碼
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "promo_codes")
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CodeType type;

    // 獎勵類型
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RewardType rewardType;

    // 獎勵數值（代幣/紅利點數/優惠券ID）
    @Column(nullable = false)
    private BigDecimal rewardValue;

    // 使用次數上限（0 = 無限）
    @Column(nullable = false)
    private Integer maxUses = 0;

    // 已使用次數
    @Column(nullable = false)
    private Integer usedCount = 0;

    // 每人限用次數
    @Column(nullable = false)
    private Integer perUserLimit = 1;

    // 有效期
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;

    // 創建者（推薦碼關聯會員ID）
    private Long creatorMemberId;

    // 是否啟用
    @Column(nullable = false)
    private boolean enabled = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum CodeType {
        REFERRAL, // 推薦碼（用戶專屬）
        GIFT, // 禮包碼（活動發放）
        PROMOTION // 促銷碼（限時活動）
    }

    public enum RewardType {
        TOKENS, // 贈送代幣
        BONUS, // 贈送紅利
        SHARDS, // 贈送碎片
        COUPON // 贈送優惠券
    }

    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        if (!enabled)
            return false;
        if (maxUses > 0 && usedCount >= maxUses)
            return false;
        if (validFrom != null && now.isBefore(validFrom))
            return false;
        if (validUntil != null && now.isAfter(validUntil))
            return false;
        return true;
    }
}
