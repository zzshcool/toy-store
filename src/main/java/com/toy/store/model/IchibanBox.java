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
 * 一番賞箱體實體
 * 每個箱體包含多個格子(Slot)和多種獎品(Prize)
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ichiban_boxes")
public class IchibanBox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ip_id", nullable = false)
    @JsonIgnore
    private GachaIp ip;

    @Column(nullable = false)
    private String name; // 箱體名稱，如：洛克人一番賞 第一彈

    @Column(length = 1000)
    private String description;

    private String imageUrl;

    @Column(nullable = false)
    private BigDecimal pricePerDraw; // 單抽價格

    @Column(nullable = false)
    private Integer maxSlots = 80; // 最大格數，預設80

    @Column(nullable = false)
    private Integer totalSlots; // 實際設定的格數

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    private LocalDateTime startTime; // 開賣時間
    private LocalDateTime endTime; // 結束時間

    private LocalDateTime createdAt = LocalDateTime.now();

    // 箱體內的格子
    @OneToMany(mappedBy = "box", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IchibanSlot> slots;

    // 箱體內的獎品種類
    @OneToMany(mappedBy = "box", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IchibanPrize> prizes;

    public enum Status {
        DRAFT, // 草稿
        ACTIVE, // 進行中
        SOLD_OUT, // 售罄
        ENDED // 已結束
    }

    // 取得 IP 名稱（方便顯示）
    public String getIpName() {
        return ip != null ? ip.getName() : "";
    }
}
