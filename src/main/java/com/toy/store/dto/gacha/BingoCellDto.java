package com.toy.store.dto.gacha;

import com.toy.store.model.BingoCell;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 九宮格格子 DTO
 */
@Data
public class BingoCellDto {
    private Long id;
    private int position;
    private int row;
    private int col;
    private boolean isRevealed;
    private String prizeName;
    private BigDecimal prizeValue;
    private String tier;
    private Long revealedByMemberId;

    public static BingoCellDto from(BingoCell cell) {
        BingoCellDto dto = new BingoCellDto();
        dto.setId(cell.getId());
        dto.setPosition(cell.getPosition());
        dto.setRow(cell.getRow());
        dto.setCol(cell.getCol());
        dto.setRevealed(cell.getIsRevealed());
        // 只有揭曉後才暴露獎品資訊
        if (cell.getIsRevealed()) {
            dto.setPrizeName(cell.getPrizeName());
            dto.setPrizeValue(cell.getPrizeValue());
            dto.setTier(cell.getTier() != null ? cell.getTier().name() : null);
        }
        dto.setRevealedByMemberId(cell.getRevealedByMemberId());
        return dto;
    }
}
