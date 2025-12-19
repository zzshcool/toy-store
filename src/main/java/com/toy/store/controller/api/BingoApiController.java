package com.toy.store.controller.api;

import com.toy.store.exception.AppException;

import com.toy.store.annotation.CurrentUser;
import com.toy.store.dto.ApiResponse;
import com.toy.store.model.*;
import com.toy.store.service.BingoService;
import com.toy.store.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 九宮格 API
 */
@RestController
@RequestMapping("/api/bingo")
public class BingoApiController {

    @Autowired
    private BingoService bingoService;

    @Autowired
    private com.toy.store.repository.MemberRepository memberRepository;

    /**
     * 取得所有進行中的九宮格遊戲
     */
    @GetMapping
    public ApiResponse<List<Map<String, Object>>> getActiveGames() {
        List<BingoGame> games = bingoService.getActiveGames();
        List<Map<String, Object>> result = games.stream().map(this::gameToMap).collect(Collectors.toList());
        return ApiResponse.ok(result);
    }

    /**
     * 取得單一遊戲詳情（含格子）
     */
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getGame(@PathVariable Long id) {
        BingoGame game = bingoService.getGameWithCells(id);
        if (game == null) {
            return ApiResponse.error("遊戲不存在");
        }
        Map<String, Object> result = gameToMap(game);
        result.put("cells", bingoService.getCells(id).stream()
                .map(this::cellToMap).collect(Collectors.toList()));
        return ApiResponse.ok(result);
    }

    /**
     * 挖掘格子
     */
    @PostMapping("/{id}/dig/{pos}")
    public ApiResponse<Map<String, Object>> dig(
            @PathVariable Long id,
            @PathVariable Integer pos,
            @CurrentUser TokenService.TokenInfo info) {

        Long memberId = getMemberId(info);
        if (memberId == null) {
            throw new AppException("請先登入");
        }

        BingoService.DigResult result = bingoService.dig(id, pos, memberId);
        Map<String, Object> response = new HashMap<>();
        response.put("cell", cellToMap(result.getCell()));
        response.put("shardsEarned", result.getShardsEarned());
        response.put("hasBingo", result.isHasBingo());
        response.put("gridSize", result.getGridSize());

        if (result.isHasBingo()) {
            response.put("bingoRewardName", result.getBingoRewardName());
            response.put("bingoLines", result.getBingoLines().stream()
                    .map(line -> {
                        Map<String, Object> lineMap = new HashMap<>();
                        lineMap.put("type", line.getType().name());
                        lineMap.put("index", line.getIndex());
                        return lineMap;
                    }).collect(Collectors.toList()));
        }

        String message = result.isHasBingo()
                ? "🎉 連線成功！恭喜獲得額外獎勵！"
                : "恭喜獲得: " + result.getCell().getPrizeName();
        return ApiResponse.ok(response, message);
    }

    private Long getMemberId(TokenService.TokenInfo info) {
        if (info == null)
            return null;
        return memberRepository.findByUsername(info.getUsername())
                .map(Member::getId)
                .orElse(null);
    }

    private Map<String, Object> gameToMap(BingoGame game) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", game.getId());
        map.put("name", game.getName());
        map.put("description", game.getDescription());
        map.put("imageUrl", game.getImageUrl());
        map.put("pricePerDig", game.getPricePerDig());
        map.put("gridSize", game.getGridSize());
        map.put("totalCells", game.getTotalCells());
        map.put("bingoRewardName", game.getBingoRewardName());
        map.put("ipName", game.getIpName());
        return map;
    }

    private Map<String, Object> cellToMap(BingoCell cell) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", cell.getId());
        map.put("position", cell.getPosition());
        map.put("row", cell.getRow());
        map.put("col", cell.getCol());
        map.put("isRevealed", cell.getIsRevealed());
        if (cell.getIsRevealed()) {
            map.put("prizeName", cell.getPrizeName());
            map.put("prizeImageUrl", cell.getPrizeImageUrl());
        }
        return map;
    }
}
