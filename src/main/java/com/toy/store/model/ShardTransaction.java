package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 碎片交易紀錄實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShardTransaction {
    private Long id;
    private Long memberId;
    private String type; // EARN, SPEND, TRANSFER
    private Integer amount;
    private Integer balanceAfter;
    private String description;
    private Long referenceId;
    private String referenceType;
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum TransactionType {
        EARN, SPEND, TRANSFER, GACHA, DUP_REFUND,
        EARN_DUPLICATE, EARN_BONUS, EARN_DRAW
    }

    // 設置類型（枚舉）
    public void setType(TransactionType type) {
        this.type = type != null ? type.name() : null;
    }

    // 獲取類型枚舉
    public TransactionType getTypeEnum() {
        try {
            return type != null ? TransactionType.valueOf(type) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // 建立消費交易
    public static ShardTransaction createSpend(Long memberId, Integer amount, String description, Long referenceId) {
        ShardTransaction tx = new ShardTransaction();
        tx.setMemberId(memberId);
        tx.setType(TransactionType.SPEND);
        tx.setAmount(-amount);
        tx.setDescription(description);
        tx.setReferenceId(referenceId);
        tx.setCreatedAt(LocalDateTime.now());
        return tx;
    }

    // 建立獲取交易
    public static ShardTransaction createEarn(Long memberId, Integer amount, TransactionType type,
            String description, String referenceType, Long referenceId) {
        ShardTransaction tx = new ShardTransaction();
        tx.setMemberId(memberId);
        tx.setType(type);
        tx.setAmount(amount);
        tx.setDescription(description);
        tx.setReferenceType(referenceType);
        tx.setReferenceId(referenceId);
        tx.setCreatedAt(LocalDateTime.now());
        return tx;
    }
}
