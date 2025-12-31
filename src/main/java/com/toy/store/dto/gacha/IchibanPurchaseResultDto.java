package com.toy.store.dto.gacha;

import com.toy.store.service.IchibanService;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 一番賞購買結果 DTO
 */
@Data
public class IchibanPurchaseResultDto {
    private List<SlotResultDto> slots;
    private BigDecimal totalCost;
    private int totalShards;

    @Data
    public static class SlotResultDto {
        private Integer slotNumber;
        private IchibanPrizeDto prize;
        private int shards;
    }

    public static IchibanPurchaseResultDto from(IchibanService.PurchaseResult result) {
        IchibanPurchaseResultDto dto = new IchibanPurchaseResultDto();
        dto.setTotalCost(result.getTotalCost());
        dto.setTotalShards(result.getTotalShards());
        dto.setSlots(result.getSlots().stream().map(slot -> {
            SlotResultDto slotDto = new SlotResultDto();
            slotDto.setSlotNumber(slot.getSlotNumber());
            if (slot.getPrize() != null) {
                slotDto.setPrize(IchibanPrizeDto.from(slot.getPrize()));
            }
            slotDto.setShards(slot.getShards());
            return slotDto;
        }).collect(Collectors.toList()));
        return dto;
    }
}
