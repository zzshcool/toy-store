package com.toy.store.dto.gacha;

import com.toy.store.service.RouletteService;
import lombok.Data;

/**
 * 轉盤旋轉結果 DTO
 */
@Data
public class RouletteSpinResultDto {
    private RouletteSlotDto winningSlot;
    private boolean isGuarantee;
    private boolean isFreeSpin;
    private int shardsEarned;
    private int currentLuckyValue;
    private int luckyThreshold;
    private int luckyPercentage;

    public static RouletteSpinResultDto from(RouletteService.SpinResult result) {
        RouletteSpinResultDto dto = new RouletteSpinResultDto();
        dto.setWinningSlot(RouletteSlotDto.from(result.getSlot()));
        dto.setGuarantee(result.isGuarantee());
        dto.setFreeSpin(result.isFreeSpin());
        dto.setShardsEarned(result.getShardsEarned());
        dto.setCurrentLuckyValue(result.getCurrentLuckyValue());
        dto.setLuckyThreshold(result.getLuckyThreshold());
        dto.setLuckyPercentage(result.getLuckyPercentage());
        return dto;
    }
}
