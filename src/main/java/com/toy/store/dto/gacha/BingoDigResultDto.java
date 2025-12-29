package com.toy.store.dto.gacha;

import com.toy.store.service.BingoService;
import lombok.Data;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 九宮格挖掘結果 DTO
 */
@Data
public class BingoDigResultDto {
    private List<BingoCellDto> cells;
    private int totalShards;
    private boolean hasBingo;
    private List<BingoLineDto> bingoLines;
    private String bingoRewardName;
    private int gridSize;

    @Data
    public static class BingoLineDto {
        private String type;
        private int index;
    }

    public static BingoDigResultDto from(BingoService.DigBatchResult result) {
        BingoDigResultDto dto = new BingoDigResultDto();
        dto.setCells(result.getCells().stream()
                .map(BingoCellDto::from)
                .collect(Collectors.toList()));
        dto.setTotalShards(result.getTotalShards());
        dto.setHasBingo(result.isHasBingo());
        dto.setBingoLines(result.getBingoLines().stream().map(line -> {
            BingoLineDto lineDto = new BingoLineDto();
            lineDto.setType(line.getType().name());
            lineDto.setIndex(line.getIndex());
            return lineDto;
        }).collect(Collectors.toList()));
        dto.setBingoRewardName(result.getBingoRewardName());
        dto.setGridSize(result.getGridSize());
        return dto;
    }
}
