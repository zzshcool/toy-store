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
    private String postalCode;
    private Boolean isFreeShipping = false;
    private String trackingNumber;
    private String shippingCompany;
    private String adminNote;
    private String statusValue = "PENDING"; // 底層 String 存儲
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Status {
        PENDING("待處理"),
        PROCESSING("處理中"),
        SHIPPED("已出貨"),
        DELIVERED("已送達"),
        CANCELLED("已取消");

        private final String displayName;

        Status(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // 保持 MyBatis 映射兼容
    public String getStatus() {
        return statusValue;
    }

    public void setStatus(String status) {
        this.statusValue = status;
    }

    // 額外的枚舉便捷方法
    public Status getStatusEnum() {
        return statusValue != null ? Status.valueOf(statusValue) : null;
    }

    public void setStatusEnum(Status status) {
        if (status != null) {
            this.statusValue = status.name();
        }
    }

    // Boolean getter
    public boolean getIsFreeShipping() {
        return isFreeShipping != null && isFreeShipping;
    }

    public void setIsFreeShipping(boolean isFreeShipping) {
        this.isFreeShipping = isFreeShipping;
    }
}
