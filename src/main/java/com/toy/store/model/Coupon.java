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
    private String description;
    private String discountType; // PERCENTAGE, FIXED_AMOUNT
    private BigDecimal discountValue;
    private BigDecimal minPurchase = BigDecimal.ZERO;
    private BigDecimal maxDiscount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer usageLimit;
    private Integer usedCount = 0;
    private String status = "ACTIVE"; // ACTIVE, INACTIVE, EXPIRED
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum CouponType {
        PERCENTAGE, FIXED_AMOUNT
    }

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    public void setActive(boolean active) {
        this.status = active ? "ACTIVE" : "INACTIVE";
    }

    // Alias for controller compatibility
    public String getType() {
        return discountType;
    }

    public void setType(String type) {
        this.discountType = type;
    }

    public BigDecimal getValue() {
        return discountValue;
    }

    public void setValue(BigDecimal value) {
        this.discountValue = value;
    }

    public LocalDateTime getValidFrom() {
        return startDate;
    }

    public void setValidFrom(LocalDateTime from) {
        this.startDate = from;
    }

    public LocalDateTime getValidUntil() {
        return endDate;
    }

    public void setValidUntil(LocalDateTime until) {
        this.endDate = until;
    }
}
