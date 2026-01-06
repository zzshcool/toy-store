package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 盲盒（中盒）實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlindBox {

    private Long id;

    private String name; // 盲盒名稱

    private String description;

    private String imageUrl;

    private String ipName; // IP 名稱（如：海賊王、火影忍者）

    private BigDecimal pricePerBox; // 單抽價格

    private BigDecimal fullBoxPrice; // 全包價格（整中盒）

    private Integer totalBoxes = 12; // 中盒內總格數（通常 6-12 個）

    private Status status = Status.ACTIVE;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private transient List<BlindBoxItem> items;

    public enum Status {
        ACTIVE, // 進行中
        SOLD_OUT, // 售罄
        INACTIVE // 下架
    }

    /**
     * 計算剩餘可選格數
     */
    public int getRemainingCount() {
        if (items == null)
            return 0;
        return (int) items.stream()
                .filter(item -> item.getStatus() == BlindBoxItem.Status.AVAILABLE)
                .count();
    }

    /**
     * 是否已售罄
     */
    public boolean isSoldOut() {
        return getRemainingCount() == 0;
    }
}
