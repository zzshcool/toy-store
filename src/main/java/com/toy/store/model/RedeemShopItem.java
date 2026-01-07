package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 兌換商店商品實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedeemShopItem {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private String itemType;
    private Integer pointCost;
    private Integer stock = 0;
    private String status = "ACTIVE";
    private Integer shardCost;
    private java.math.BigDecimal estimatedValue;
    private Integer totalStock;
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum ItemType {
        COUPON, PROP_CARD, PHYSICAL, VIRTUAL
    }

    public enum Status {
        ACTIVE, INACTIVE
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType != null ? itemType.name() : null;
    }

    public void setStatus(Status status) {
        this.status = status != null ? status.name() : null;
    }

    public ItemType getItemTypeEnum() {
        try {
            return itemType != null ? ItemType.valueOf(itemType) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public Status getStatusEnum() {
        try {
            return status != null ? Status.valueOf(status) : Status.ACTIVE;
        } catch (IllegalArgumentException e) {
            return Status.ACTIVE;
        }
    }

    // 是否有庫存
    public boolean hasStock() {
        return stock == null || stock > 0;
    }

    // 減少庫存
    public void decreaseStock() {
        if (stock != null && stock > 0) {
            stock--;
        }
    }

    // 取得庫存百分比
    public int getStockPercentage() {
        if (totalStock == null || totalStock == 0) {
            return 100;
        }
        return (stock != null ? stock : 0) * 100 / totalStock;
    }
}
