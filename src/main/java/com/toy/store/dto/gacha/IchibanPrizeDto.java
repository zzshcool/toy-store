package com.toy.store.dto.gacha;

import com.toy.store.model.IchibanPrize;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 一番賞獎品 DTO
 */
@Data
public class IchibanPrizeDto {
    private Long id;
    private String name;
    private String rank;
    private String imageUrl;
    private BigDecimal estimatedValue;
    private int totalQuantity;
    private int remainingQuantity;

    public static IchibanPrizeDto from(IchibanPrize prize) {
        IchibanPrizeDto dto = new IchibanPrizeDto();
        dto.setId(prize.getId());
        dto.setName(prize.getName());
        dto.setRank(prize.getRank() != null ? prize.getRank().name() : null);
        dto.setImageUrl(prize.getImageUrl());
        dto.setEstimatedValue(prize.getEstimatedValue());
        dto.setTotalQuantity(prize.getTotalQuantity());
        dto.setRemainingQuantity(prize.getRemainingQuantity());
        return dto;
    }
}
