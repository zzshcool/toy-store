package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 九宮格遊戲實體 - 純 POJO (MyBatis)
 * 支援 3×3 到 6×6 的網格大小
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BingoGame {

    private Long id;

    private Long ipId;
    private transient GachaIp ip;

    private String name;

    private String description;

    private String imageUrl;

    private BigDecimal pricePerDig; // 單次挖掘價格

    private Integer gridSize = 3; // 網格大小：3-6（表示 3×3 到 6×6）

    private Status status = Status.ACTIVE;

    // 連線獎勵設定
    private String bingoRewardName; // 連線獎勵名稱
    private String bingoRewardImageUrl;
    private BigDecimal bingoRewardValue;

    private LocalDateTime createdAt = LocalDateTime.now();

    // 格子（非持久化，由 Service 層填充）
    private transient List<BingoCell> cells;

    public enum Status {
        DRAFT,
        ACTIVE,
        INACTIVE
    }

    // 取得總格數
    public int getTotalCells() {
        return gridSize * gridSize;
    }

    // 取得 IP 名稱
    public String getIpName() {
        return ip != null ? ip.getName() : "";
    }

    // 驗證 gridSize 範圍
    public void validateGridSize() {
        if (gridSize < 3)
            gridSize = 3;
        if (gridSize > 6)
            gridSize = 6;
    }
}
