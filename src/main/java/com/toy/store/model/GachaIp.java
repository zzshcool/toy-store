package com.toy.store.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * IP 主題實體 - 所有抽獎遊戲的核心
 * 例如：洛克人、航海王、鬼滅之刃等授權IP
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "gacha_ips")
public class GachaIp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // IP 名稱，如：洛克人

    @Column(length = 1000)
    private String description;

    private String imageUrl; // IP 主視覺圖

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    // 關聯的一番賞箱體
    @OneToMany(mappedBy = "ip", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IchibanBox> ichibanBoxes;

    // 關聯的轉盤遊戲
    @OneToMany(mappedBy = "ip", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RouletteGame> rouletteGames;

    // 關聯的九宮格遊戲
    @OneToMany(mappedBy = "ip", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BingoGame> bingoGames;

    // 關聯的扭蛋主題
    @OneToMany(mappedBy = "ip", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GachaTheme> gachaThemes;

    public enum Status {
        ACTIVE, // 啟用中
        INACTIVE, // 停用
        COMING_SOON // 即將推出
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
