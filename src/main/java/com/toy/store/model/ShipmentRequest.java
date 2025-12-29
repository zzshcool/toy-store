package com.toy.store.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 發貨申請實體
 * 對應規格書 §8.A - 滿 5 件免運，手動申請發貨
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "shipment_requests")
public class ShipmentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    // 收件資訊
    @Column(nullable = false)
    private String recipientName;

    @Column(nullable = false)
    private String recipientPhone;

    @Column(nullable = false)
    private String recipientAddress;

    private String postalCode;

    // 發貨資訊
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(nullable = false)
    private Integer itemCount; // 包裹內獎品數量

    private Boolean isFreeShipping = false; // 是否免運（滿 5 件）

    private java.math.BigDecimal shippingFee; // 運費

    private String trackingNumber; // 物流單號
    private String shippingCompany; // 物流公司
    private String adminNote; // 後台備註

    private LocalDateTime createdAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;

    @OneToMany
    @JoinColumn(name = "shipmentRequestId")
    private List<CabinetItem> items;

    public enum Status {
        PENDING("待處理"),
        PROCESSING("處理中"),
        SHIPPED("已發貨"),
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

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        // 滿 5 件免運
        if (itemCount != null && itemCount >= 5) {
            isFreeShipping = true;
            shippingFee = java.math.BigDecimal.ZERO;
        }
    }

    /**
     * 標記為已發貨
     */
    public void markShipped(String trackingNumber, String company) {
        this.status = Status.SHIPPED;
        this.trackingNumber = trackingNumber;
        this.shippingCompany = company;
        this.shippedAt = LocalDateTime.now();
    }

    /**
     * 標記為已送達
     */
    public void markDelivered() {
        this.status = Status.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }
}
