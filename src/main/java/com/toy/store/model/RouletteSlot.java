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

    private String slotType = "NORMAL";
    private Integer slotOrder;
    private Integer shardAmount;

    public enum SlotType {
        NORMAL, RARE, JACKPOT, FREE_SPIN, PRIZE, BONUS, EMPTY, SHARD;

        public String getDisplayName() {
            switch (this) {
                case NORMAL:
                    return "普通";
                case RARE:
                    return "稀有";
                case JACKPOT:
                    return "大獎";
                case FREE_SPIN:
                    return "免費再轉";
                case PRIZE:
                    return "獎品";
                case BONUS:
                    return "額外獎勵";
                case EMPTY:
                    return "空";
                case SHARD:
                    return "碎片";
                default:
                    return name();
            }
        }
    }

    public String getSlotType() {
        return slotType;
    }

    public SlotType getSlotTypeEnum() {
        try {
            return slotType != null ? SlotType.valueOf(slotType) : SlotType.NORMAL;
        } catch (IllegalArgumentException e) {
            return SlotType.NORMAL;
        }
    }

    public void setSlotType(SlotType type) {
        this.slotType = type != null ? type.name() : null;
    }

    public Integer getSlotOrder() {
        return slotOrder;
    }

    public void setSlotOrder(int order) {
        this.slotOrder = order;
    }

    public Integer getShardAmount() {
        return shardAmount;
    }

    public void setShardAmount(int amount) {
        this.shardAmount = amount;
    }

    // 取得顏色（根據 tier）
    public String getColor() {
        if (tier == null)
            return "#888888";
        switch (tier) {
            case JACKPOT:
                return "#FFD700"; // 金色
            case RARE:
                return "#9B59B6"; // 紫色
            case NORMAL:
                return "#3498DB"; // 藍色
            default:
                return "#888888";
        }
    }

    // 是否為大獎
    public boolean isJackpot() {
        return tier == GachaProbabilityEngine.PrizeTier.JACKPOT ||
                (slotType != null && "JACKPOT".equals(slotType));
    }
}
