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
}
