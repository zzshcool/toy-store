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
        A, B, C, D, E, F, G, LAST
    }
}
