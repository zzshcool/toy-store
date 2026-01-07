package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

/**
 * 一番賞獎品實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IchibanPrize {

    private Long id;

    private Long boxId; // 箱體 ID（手動關聯）
    private transient IchibanBox box;

    private Rank rank; // A、B、C、D、E 等

    private String name;

    private String description;

    private String imageUrl;

    private Integer quantity; // 總數量

    private Integer remainingQuantity; // 剩餘數量

    private BigDecimal estimatedValue; // 估計價值

    private Integer shardsValue = 0; // 碎片價值

    public enum Rank {
        A, B, C, D, E, F, G, LAST;

        public int getOrder() {
            return ordinal();
        }

        public String getDisplayName() {
            if (this == LAST) {
                return "最終賞";
            }
            return name() + "賞";
        }
    }

    public Integer getTotalQuantity() {
        return quantity;
    }

    private Integer sortOrder;

    public void setSortOrder(int order) {
        this.sortOrder = order;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    // 減少剩餘數量
    public void decreaseQuantity() {
        if (remainingQuantity != null && remainingQuantity > 0) {
            remainingQuantity--;
        }
    }
}
