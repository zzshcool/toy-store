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
}
