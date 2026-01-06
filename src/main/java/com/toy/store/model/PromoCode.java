package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 促銷碼實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromoCode {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String discountType; // PERCENTAGE, FIXED
    private BigDecimal discountValue;
    private BigDecimal minPurchase = BigDecimal.ZERO;
    private BigDecimal maxDiscount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer usageLimit;
    private Integer perUserLimit = 1;
    private Integer usedCount = 0;
    private String status = "ACTIVE";
}
