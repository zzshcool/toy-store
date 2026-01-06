package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.toy.store.service.GachaProbabilityEngine;

/**
 * 轉盤格子實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouletteSlot implements GachaProbabilityEngine.ProbableItem {

    private Long id;

    private Long gameId;
    private transient RouletteGame game;

    private Integer position;

    private String prizeName;

    private String prizeDescription;

    private String prizeImageUrl;

    private BigDecimal prizeValue;

    private Integer weight = 1;

    private GachaProbabilityEngine.PrizeTier tier = GachaProbabilityEngine.PrizeTier.NORMAL;

    private Integer shardsReward = 0;

    private Integer luckyValueReward = 0;

    @Override
    public Integer getWeight() {
        return this.weight != null ? this.weight : 1;
    }

    @Override
    public GachaProbabilityEngine.PrizeTier getTier() {
        return this.tier != null ? this.tier : GachaProbabilityEngine.PrizeTier.NORMAL;
    }
}
