package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 促銷碼/推薦碼實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromoCode {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String type; // REFERRAL, GIFT, DISCOUNT
    private String rewardType; // TOKENS, BONUS, SHARDS, DISCOUNT
    private BigDecimal rewardValue;
    private BigDecimal minPurchase = BigDecimal.ZERO;
    private BigDecimal maxDiscount;
    private LocalDateTime validUntil;
    private Integer maxUses = 0; // 0 = 無限
    private Integer perUserLimit = 1;
    private Integer usedCount = 0;
    private Long creatorMemberId;
    private Boolean enabled = true;
    private LocalDateTime createdAt = LocalDateTime.now();

    // 枚舉類型
    public enum CodeType {
        REFERRAL, GIFT, DISCOUNT
    }

    public enum RewardType {
        TOKENS, BONUS, SHARDS, DISCOUNT
    }

    // 便捷方法
    public CodeType getCodeType() {
        try {
            return type != null ? CodeType.valueOf(type) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void setCodeType(CodeType codeType) {
        this.type = codeType != null ? codeType.name() : null;
    }

    public RewardType getRewardTypeEnum() {
        try {
            return rewardType != null ? RewardType.valueOf(rewardType) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void setRewardTypeEnum(RewardType reward) {
        this.rewardType = reward != null ? reward.name() : null;
    }

    public boolean isValid() {
        if (!Boolean.TRUE.equals(enabled)) {
            return false;
        }
        if (validUntil != null && validUntil.isBefore(LocalDateTime.now())) {
            return false;
        }
        if (maxUses > 0 && usedCount >= maxUses) {
            return false;
        }
        return true;
    }
}
