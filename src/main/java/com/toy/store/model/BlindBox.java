package com.toy.store.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 盲盒（中盒）實體
 * 對應規格書 §4.D 動漫周邊系統
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "blind_boxes")
public class BlindBox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 盲盒名稱

    @Column(length = 500)
    private String description;

    private String imageUrl;

    @Column(nullable = false)
    private String ipName; // IP 名稱（如：海賊王、火影忍者）

    @Column(nullable = false)
    private BigDecimal pricePerBox; // 單抽價格

    @Column(nullable = false)
    private BigDecimal fullBoxPrice; // 全包價格（整中盒）

    @Column(nullable = false)
    private Integer totalBoxes = 12; // 中盒內總格數（通常 6-12 個）

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "blindBox", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BlindBoxItem> items;

    public enum Status {
        ACTIVE, // 進行中
        SOLD_OUT, // 售罄
        INACTIVE // 下架
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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
