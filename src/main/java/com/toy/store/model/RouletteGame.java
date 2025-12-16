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
 * 轉盤遊戲實體
 * 每個轉盤最多支援 25 個獎格
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "roulette_games")
public class RouletteGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ip_id", nullable = false)
    @JsonIgnore
    private GachaIp ip;

    @Column(nullable = false)
    private String name; // 轉盤名稱

    @Column(length = 1000)
    private String description;

    private String imageUrl; // 轉盤背景圖

    @Column(nullable = false)
    private BigDecimal pricePerSpin; // 單次旋轉價格

    @Column(nullable = false)
    private Integer maxSlots = 25; // 最大格數限制

    @Column(nullable = false)
    private Integer totalSlots; // 實際設定的格數

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    private LocalDateTime createdAt = LocalDateTime.now();

    // 轉盤上的獎格
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("slotOrder ASC")
    private List<RouletteSlot> slots;

    public enum Status {
        DRAFT,
        ACTIVE,
        INACTIVE
    }

    // 取得 IP 名稱
    public String getIpName() {
        return ip != null ? ip.getName() : "";
    }

    // 計算每格所佔角度
    public double getSlotAngle() {
        return totalSlots > 0 ? 360.0 / totalSlots : 0;
    }
}
