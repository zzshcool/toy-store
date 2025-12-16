package com.toy.store.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 九宮格遊戲實體
 * 支援 3×3 到 6×6 的網格大小
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bingo_games")
public class BingoGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ip_id", nullable = false)
    @JsonIgnore
    private GachaIp ip;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    private String imageUrl;

    @Column(nullable = false)
    private BigDecimal pricePerDig; // 單次挖掘價格

    @Column(nullable = false)
    private Integer gridSize = 3; // 網格大小：3-6（表示 3×3 到 6×6）

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    // 連線獎勵設定
    private String bingoRewardName; // 連線獎勵名稱
    private String bingoRewardImageUrl;
    private BigDecimal bingoRewardValue;

    private LocalDateTime createdAt = LocalDateTime.now();

    // 格子
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("position ASC")
    private List<BingoCell> cells;

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
    @PrePersist
    @PreUpdate
    public void validateGridSize() {
        if (gridSize < 3)
            gridSize = 3;
        if (gridSize > 6)
            gridSize = 6;
    }
}
