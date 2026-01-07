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

    private SourceType sourceType; // 來源類型

    private Long sourceId; // 來源 ID

    private String itemName;

    private String itemDescription;

    private String itemImageUrl;

    private String itemRank;

    private BigDecimal itemValue;

    private Status status = Status.IN_CABINET;

    private Long shipmentRequestId;

    private LocalDateTime requestedAt;

    private LocalDateTime obtainedAt = LocalDateTime.now();

    private LocalDateTime shippedAt;

    public enum SourceType {
        ICHIBAN("一番賞"),
        ROULETTE("轉盤"),
        BINGO("九宮格"),
        BLIND_BOX("盲盒");

        private final String displayName;

        SourceType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Status {
        IN_CABINET, // 在置物櫃中
        PENDING_SHIPMENT, // 申請出貨中
        SHIPPED, // 已出貨
        DELIVERED, // 已送達
        EXCHANGED, // 已兌換
        PENDING_SHIP // 兼容別名
    }

    public String getPrizeRank() {
        return itemRank;
    }

    public void setPrizeRank(String prizeRank) {
        this.itemRank = prizeRank;
    }

    public String getPrizeName() {
        return itemName;
    }

    public void setPrizeName(String prizeName) {
        this.itemName = prizeName;
    }

    public String getPrizeDescription() {
        return itemDescription;
    }

    public void setPrizeDescription(String prizeDescription) {
        this.itemDescription = prizeDescription;
    }

    public String getPrizeImageUrl() {
        return itemImageUrl;
    }

    public void setPrizeImageUrl(String prizeImageUrl) {
        this.itemImageUrl = prizeImageUrl;
    }

    public String getStatusDisplayName() {
        if (status == null)
            return "";
        switch (status) {
            case IN_CABINET:
                return "在置物櫃中";
            case PENDING_SHIPMENT:
                return "申請出貨中";
            case SHIPPED:
                return "已出貨";
            case DELIVERED:
                return "已送達";
            case EXCHANGED:
                return "已兌換";
            case PENDING_SHIP:
                return "待出貨";
            default:
                return status.name();
        }
    }
}
