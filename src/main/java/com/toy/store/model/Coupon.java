package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 優惠券實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {
    private Long id;
    private String name;
    private String code;
    private String discountType; // PERCENTAGE, FIXED
    private BigDecimal discountValue;
    private BigDecimal minPurchase = BigDecimal.ZERO;
    private BigDecimal maxDiscount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer usageLimit;
    private Integer usedCount = 0;
    private String status = "ACTIVE";
}
