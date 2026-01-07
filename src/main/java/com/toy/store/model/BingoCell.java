package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.toy.store.service.GachaProbabilityEngine;

/**
 * 九宮格格子實體 - 純 POJO (MyBatis)
 * 每個格子代表網格中的一個位置
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BingoCell implements GachaProbabilityEngine.ProbableItem {

    private Long id;

    private Long gameId;
    private transient BingoGame game;

    private Integer position; // 位置編號（從1開始，左上到右下）

    private Integer row; // 行（0-5）

    private Integer col; // 列（0-5）

    private String prizeName;

    private String prizeDescription;
    private String prizeImageUrl;
    private BigDecimal prizeValue;

    private Boolean isRevealed = false;

    private GachaProbabilityEngine.PrizeTier tier = GachaProbabilityEngine.PrizeTier.NORMAL;

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

    @Override
    public Integer getWeight() {
        return 1;
    }

    @Override
    public GachaProbabilityEngine.PrizeTier getTier() {
        return this.tier != null ? this.tier : GachaProbabilityEngine.PrizeTier.NORMAL;
    }

    public void setTier(GachaProbabilityEngine.PrizeTier tier) {
        this.tier = tier;
    }

    public Long getGameId() {
        return gameId != null ? gameId : (game != null ? game.getId() : null);
    }

    // 根據位置計算行列（用於建立格子時）
    public void calculateRowCol(int gridSize) {
        this.row = (position - 1) / gridSize;
        this.col = (position - 1) % gridSize;
    }

    // Boolean getter 兼容
    public Boolean getIsRevealed() {
        return isRevealed != null && isRevealed;
    }
}
