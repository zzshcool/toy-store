package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 置物櫃物品實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CabinetItem {

    private Long id;

    private Long memberId;

    private String sourceType; // 來源類型：ICHIBAN, ROULETTE, BINGO, BLIND_BOX

    private Long sourceId; // 來源 ID

    private String itemName;

    private String itemDescription;

    private String itemImageUrl;

    private BigDecimal itemValue;

    private Status status = Status.IN_CABINET;

    private LocalDateTime obtainedAt = LocalDateTime.now();

    private LocalDateTime shippedAt;

    public enum Status {
        IN_CABINET, // 在置物櫃中
        PENDING_SHIPMENT, // 申請出貨中
        SHIPPED, // 已出貨
        DELIVERED // 已送達
    }
}
