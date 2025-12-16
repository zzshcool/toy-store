package com.toy.store.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 九宮格格子實體
 * 每個格子代表網格中的一個位置
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bingo_cells")
public class BingoCell {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    @JsonIgnore
    private BingoGame game;

    @Column(nullable = false)
    private Integer position; // 位置編號（從1開始，左上到右下）

    @Column(name = "row_num", nullable = false)
    private Integer row; // 行（0-5）

    @Column(name = "col_num", nullable = false)
    private Integer col; // 列（0-5）

    @Column(nullable = false)
    private String prizeName;

    private String prizeDescription;
    private String prizeImageUrl;
    private BigDecimal prizeValue;

    @Column(nullable = false)
    private Boolean isRevealed = false;

    // 揭曉資訊
    private Long revealedByMemberId;
    private LocalDateTime revealedAt;

    // 挖掘並揭曉
    public void dig(Long memberId) {
        this.isRevealed = true;
        this.revealedByMemberId = memberId;
        this.revealedAt = LocalDateTime.now();
    }

    // 取得遊戲名稱
    public String getGameName() {
        return game != null ? game.getName() : "";
    }

    // 根據位置計算行列（用於建立格子時）
    public void calculateRowCol(int gridSize) {
        this.row = (position - 1) / gridSize;
        this.col = (position - 1) % gridSize;
    }
}
