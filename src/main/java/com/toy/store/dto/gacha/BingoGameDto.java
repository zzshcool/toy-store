package com.toy.store.dto.gacha;

import com.toy.store.model.BingoGame;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 九宮格遊戲 DTO
 */
@Data
public class BingoGameDto {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal pricePerDig;
    private int gridSize;
    private int totalCells;
    private String bingoRewardName;
    private String status;
    private String ipName;
    private LocalDateTime createdAt;

    public static BingoGameDto from(BingoGame game) {
        BingoGameDto dto = new BingoGameDto();
        dto.setId(game.getId());
        dto.setName(game.getName());
        dto.setDescription(game.getDescription());
//        dto.setImageUrl(game.getThumbnailUrl());
        dto.setPricePerDig(game.getPricePerDig());
        dto.setGridSize(game.getGridSize());
        dto.setTotalCells(game.getTotalCells());
        dto.setBingoRewardName(game.getBingoRewardName());
        dto.setStatus(game.getStatus() != null ? game.getStatus().name() : null);
        dto.setIpName(game.getIp() != null ? game.getIp().getName() : null);
        dto.setCreatedAt(game.getCreatedAt());
        return dto;
    }
}
