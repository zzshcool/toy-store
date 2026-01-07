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
    private LocalDateTime lastUpdate;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum LogisticsProvider {
        POSTAL, SF_EXPRESS, BLACK_CAT, CONVENIENCE_STORE, TCAT
    }

    public enum LogisticsStatus {
        PENDING, IN_TRANSIT, SHIPPED, DELIVERED, RETURNED
    }

    public void setStatus(LogisticsStatus status) {
        if (status != null) {
            this.status = status.name();
        }
    }

    public void setProvider(LogisticsProvider provider) {
        this.carrier = provider != null ? provider.name() : null;
    }

    public void setShipmentId(Long shipmentId) {
        this.shipmentRequestId = shipmentId;
    }
}
