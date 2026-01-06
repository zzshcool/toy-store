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
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum ItemType {
        COUPON, PROP_CARD, PHYSICAL, VIRTUAL
    }
}
