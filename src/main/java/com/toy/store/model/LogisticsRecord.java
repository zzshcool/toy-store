package com.toy.store.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 物流追蹤記錄
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "logistics_records")
public class LogisticsRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shipment_id", nullable = false)
    private Long shipmentId;

    @Column(nullable = false)
    private String trackingNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LogisticsProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LogisticsStatus status = LogisticsStatus.PENDING;

    // 收件人資訊
    private String recipientName;
    private String recipientPhone;
    private String recipientAddress;

    // 物流更新時間
    private LocalDateTime lastUpdate;

    // 預計送達時間
    private LocalDateTime estimatedDelivery;

    // 實際送達時間
    private LocalDateTime deliveredAt;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum LogisticsProvider {
        TCAT, // 黑貓宅急便
        SEVEN, // 7-11 交貨便
        FAMILY, // 全家店到店
        HILIFE, // 萊爾富
        POST // 郵局
    }

    public enum LogisticsStatus {
        PENDING, // 待出貨
        SHIPPED, // 已出貨
        IN_TRANSIT, // 配送中
        ARRIVED, // 已到站/門市
        DELIVERED, // 已送達
        RETURNED, // 已退回
        EXCEPTION // 異常
    }
}
