package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 轉盤遊戲實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouletteGame {

    private Long id;

    private Long ipId;
    private transient GachaIp ip;

    private String name;

    private String description;

    private String imageUrl;

    private BigDecimal pricePerSpin; // 單抽價格

    private Status status = Status.ACTIVE;

    private Integer guaranteeSpins = 10; // 保底次數

    private LocalDateTime createdAt = LocalDateTime.now();

    private transient List<RouletteSlot> slots;

    public enum Status {
        DRAFT,
        ACTIVE,
        INACTIVE
    }

    public String getIpName() {
        return ip != null ? ip.getName() : "";
    }
}
