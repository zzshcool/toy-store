package com.toy.store.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;

/**
 * 一番賞獎品實體
 * 定義獎品等級、名稱、數量等
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ichiban_prizes")
public class IchibanPrize {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "box_id", nullable = false)
    @JsonIgnore
    private IchibanBox box;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rank rank; // 獎品等級

    @Column(nullable = false)
    private String name; // 獎品名稱，如：手帕、公仔

    @Column(length = 500)
    private String description;

    private String imageUrl;

    private BigDecimal estimatedValue; // 估計價值

    @Column(nullable = false)
    private Integer totalQuantity; // 總數量

    @Column(nullable = false)
    private Integer remainingQuantity; // 剩餘數量

    private Integer sortOrder = 0; // 排序順序

    public enum Rank {
        A("A賞", 1),
        B("B賞", 2),
        C("C賞", 3),
        D("D賞", 4),
        E("E賞", 5),
        F("F賞", 6),
        G("G賞", 7),
        H("H賞", 8),
        LAST("LAST賞", 99);

        private final String displayName;
        private final int order;

        Rank(String displayName, int order) {
            this.displayName = displayName;
            this.order = order;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getOrder() {
            return order;
        }
    }

    // 減少剩餘數量
    public boolean decreaseQuantity() {
        if (remainingQuantity > 0) {
            remainingQuantity--;
            return true;
        }
        return false;
    }

    // 是否還有庫存
    public boolean hasStock() {
        return remainingQuantity > 0;
    }

    // 取得箱體名稱
    public String getBoxName() {
        return box != null ? box.getName() : "";
    }
}
