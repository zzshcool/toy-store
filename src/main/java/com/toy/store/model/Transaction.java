package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易紀錄實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    private Long id;

    private Long memberId;

    private String type;

    private BigDecimal amount;

    private BigDecimal balanceAfter;

    private String description;

    private Long referenceId;

    private String referenceType;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Type {
        RECHARGE, PURCHASE, REFUND, BONUS, WITHDRAW
    }
}
