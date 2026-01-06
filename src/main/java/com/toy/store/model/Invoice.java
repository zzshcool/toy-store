package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 發票實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {
    private Long id;
    private Long memberId;
    private Long paymentOrderId;
    private String invoiceNo;
    private String invoiceType;
    private String carrierType;
    private String carrierNo;
    private String buyerName;
    private String buyerTaxId;
    private BigDecimal amount;
    private String status = "PENDING";
    private LocalDateTime createdAt = LocalDateTime.now();
}
