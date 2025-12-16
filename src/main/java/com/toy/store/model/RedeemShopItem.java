package com.toy.store.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 兌換商店商品實體
 * 玩家可使用碎片兌換的獎品
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "redeem_shop_items")
public class RedeemShopItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 商品名稱

    @Column(length = 1000)
    private String description;

    private String imageUrl;

    @Column(nullable = false)
    private Integer shardCost; // 所需碎片數（如 10000）

    private BigDecimal estimatedValue; // 估計價值

    @Column(nullable = false)
    private Integer stock; // 庫存數量

    @Column(nullable = false)
    private Integer totalStock; // 總庫存（用於顯示）

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemType itemType = ItemType.PRIZE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    private Integer sortOrder = 0;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum ItemType {
        S_RANK("S賞"),
        HIDDEN("隱藏款"),
        SPECIAL("特別款"),
        PRIZE("一般獎品");

        private final String displayName;

        ItemType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Status {
        ACTIVE,
        SOLD_OUT,
        INACTIVE
    }

    // 是否有庫存
    public boolean hasStock() {
        return stock > 0;
    }

    // 減少庫存
    public boolean decreaseStock() {
        if (stock > 0) {
            stock--;
            if (stock == 0) {
                status = Status.SOLD_OUT;
            }
            return true;
        }
        return false;
    }

    // 取得庫存百分比
    public int getStockPercentage() {
        if (totalStock == 0)
            return 0;
        return (int) ((stock * 100.0) / totalStock);
    }
}
