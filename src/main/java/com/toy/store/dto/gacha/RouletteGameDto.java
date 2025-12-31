package com.toy.store.dto.gacha;

import com.toy.store.model.RouletteGame;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 轉盤遊戲 DTO
 */
@Data
public class RouletteGameDto {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal pricePerSpin;
    private int totalDraws;
    private String status;
    private String ipName;
    private LocalDateTime createdAt;

    public static RouletteGameDto from(RouletteGame game) {
        RouletteGameDto dto = new RouletteGameDto();
        dto.setId(game.getId());
        dto.setName(game.getName());
        dto.setDescription(game.getDescription());
//        dto.setImageUrl(game.getThumbnailUrl());
        dto.setPricePerSpin(game.getPricePerSpin());
        dto.setTotalDraws(game.getTotalDraws());
        dto.setStatus(game.getStatus() != null ? game.getStatus().name() : null);
        dto.setIpName(game.getIp() != null ? game.getIp().getName() : null);
        dto.setCreatedAt(game.getCreatedAt());
        return dto;
    }
}
