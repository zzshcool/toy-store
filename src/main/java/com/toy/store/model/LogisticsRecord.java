package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 物流紀錄實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogisticsRecord {
    private Long id;
    private Long memberId;
    private Long shipmentRequestId;
    private String trackingNo;
    private String carrier;
    private String status = "PENDING";
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt = LocalDateTime.now();
}
