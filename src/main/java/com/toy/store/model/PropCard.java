package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 道具卡實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropCard {
    private Long id;
    private Long memberId;
    private String cardType;
    private String cardName;
    private String description;
    private BigDecimal effectValue;
    private LocalDateTime expireAt;
    private String status = "AVAILABLE"; // AVAILABLE, USED, EXPIRED
    private LocalDateTime usedAt;
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum CardType {
        DOUBLE_PRIZE, PICK_BOX, LOCK_BOX, REVEAL_ALL, EXTRA_DRAW,
        HINT, PEEK, SWAP
    }

    public CardType getCardTypeEnum() {
        try {
            return cardType != null ? CardType.valueOf(cardType) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void setCardType(CardType type) {
        this.cardType = type != null ? type.name() : null;
    }

    // 使用卡片
    public boolean use() {
        if ("USED".equals(this.status) || "EXPIRED".equals(this.status)) {
            return false;
        }
        this.status = "USED";
        this.usedAt = LocalDateTime.now();
        return true;
    }
}
