package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 出貨申請實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentRequest {
    private Long id;
    private Long memberId;
    private String requestNo;
    private Integer itemCount = 0;
    private BigDecimal shippingFee = BigDecimal.ZERO;
    private String recipientName;
    private String recipientPhone;
    private String recipientAddress;
    private String status = "PENDING"; // PENDING, PROCESSING, SHIPPED, DELIVERED
    private LocalDateTime createdAt = LocalDateTime.now();
}
