package com.toy.store.dto.gacha;

import com.toy.store.model.RouletteSlot;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 轉盤格子 DTO
 */
@Data
public class RouletteSlotDto {
    private Long id;
    private int slotOrder;
    private String prizeName;
    private String slotType;
    private BigDecimal prizeValue;
    private String color;

    public static RouletteSlotDto from(RouletteSlot slot) {
        RouletteSlotDto dto = new RouletteSlotDto();
        dto.setId(slot.getId());
        dto.setSlotOrder(slot.getSlotOrder());
        dto.setPrizeName(slot.getPrizeName());
        dto.setSlotType(slot.getSlotType() != null ? slot.getSlotType().name() : null);
        dto.setPrizeValue(slot.getPrizeValue());
        dto.setColor(slot.getColor());
        return dto;
    }
}
