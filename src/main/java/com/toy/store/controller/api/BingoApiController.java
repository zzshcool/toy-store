package com.toy.store.controller.api;

import com.toy.store.annotation.CurrentUser;
import com.toy.store.dto.ApiResponse;
import com.toy.store.exception.AppException;
import com.toy.store.model.*;
import com.toy.store.repository.MemberRepository;
import com.toy.store.service.BingoService;
import com.toy.store.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 九宮格 API
 */
@RestController
@RequestMapping("/api/bingo")
@RequiredArgsConstructor
public class BingoApiController {

    private final BingoService bingoService;
    private final MemberRepository memberRepository;

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> getActiveGames() {
        List<BingoGame> games = bingoService.getActiveGames();
        List<Map<String, Object>> result = games.stream().map(this::gameToMap).collect(Collectors.toList());
        return ApiResponse.ok(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getGame(@PathVariable Long id) {
        BingoGame game = bingoService.getGameWithCells(id);
        if (game == null) {
            return ApiResponse.error("遊戲不存在");
        }
        Map<String, Object> result = gameToMap(game);
        result.put("cells", bingoService.getCells(id).stream()
                .map(this::cellToMap).collect(Collectors.toList()));

        List<Map<String, String>> prizePreview = bingoService.getCells(id).stream()
                .filter(c -> c.getPrizeName() != null)
                .map(c -> {
                    Map<String, String> m = new HashMap<>();
                    m.put("name", c.getPrizeName());
                    m.put("description", c.getPrizeDescription());
                    m.put("imageUrl", c.getPrizeImageUrl());
                    return m;
                })
                .distinct()
                .collect(Collectors.toList());
        result.put("prizes", prizePreview);

        return ApiResponse.ok(result);
    }

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
                    .map(this::lineToMap).collect(Collectors.toList()));
        }

        String message = result.isHasBingo()
                ? "🎉 連線成功！恭喜獲得額外獎勵！"
                : "恭喜獲得: " + result.getCell().getPrizeName();
        return ApiResponse.ok(response, message);
    }

    @PostMapping("/{id}/dig-batch")
    public ApiResponse<Map<String, Object>> digBatch(
            @PathVariable Long id,
            @RequestBody Map<String, List<Integer>> payload,
            @CurrentUser TokenService.TokenInfo info) {

        Long memberId = getMemberId(info);
        if (memberId == null) {
            throw new AppException("請先登入");
        }

        List<Integer> positions = payload.get("positions");
        if (positions == null || positions.isEmpty()) {
            throw new AppException("請選擇欲挖掘的格子");
        }

        BingoService.DigBatchResult result = bingoService.digMultiple(id, positions, memberId);
        Map<String, Object> response = new HashMap<>();
        response.put("cells", result.getCells().stream().map(this::cellToMap).collect(Collectors.toList()));
        response.put("totalShards", result.getTotalShards());
        response.put("hasBingo", result.isHasBingo());
        response.put("gridSize", result.getGridSize());

        if (result.isHasBingo()) {
            response.put("bingoRewardName", result.getBingoRewardName());
            response.put("bingoLines", result.getBingoLines().stream()
                    .map(this::lineToMap).collect(Collectors.toList()));
        }

        String message = result.isHasBingo()
                ? "🎉 挖得漂亮！且連線成功！"
                : "成功開箱 " + positions.size() + " 個格子！";
        return ApiResponse.ok(response, message);
    }

    private Map<String, Object> lineToMap(BingoService.BingoLine line) {
        Map<String, Object> lineMap = new HashMap<>();
        lineMap.put("type", line.getType().name());
        lineMap.put("index", line.getIndex());
        return lineMap;
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

    // ============== 試抽功能 ==============

    @PostMapping("/{id}/trial")
    public ApiResponse<Map<String, Object>> trial(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Integer> body) {

        BingoGame game = bingoService.getGameWithCells(id);
        if (game == null) {
            return ApiResponse.error("遊戲不存在");
        }

        List<BingoCell> cells = bingoService.getCells(id);
        List<BingoCell> availableCells = cells.stream()
                .filter(c -> !c.getIsRevealed())
                .collect(Collectors.toList());

        if (availableCells.isEmpty()) {
            return ApiResponse.error("此遊戲已結束，無法試挖");
        }

        int count = 1;
        if (body != null && body.containsKey("count")) {
            count = Math.max(1, Math.min(availableCells.size(), body.get("count")));
        }

        Random random = new Random();
        List<Map<String, Object>> results = new ArrayList<>();
        Set<Integer> usedIndexes = new HashSet<>();

        for (int i = 0; i < count; i++) {
            int index;
            do {
                index = random.nextInt(availableCells.size());
            } while (usedIndexes.contains(index) && usedIndexes.size() < availableCells.size());
            usedIndexes.add(index);

            BingoCell cell = availableCells.get(index);
            int mockShards = random.nextInt(20) + 1;

            Map<String, Object> result = new HashMap<>();
            result.put("position", cell.getPosition());
            result.put("row", cell.getRow());
            result.put("col", cell.getCol());
            result.put("prizeName", cell.getPrizeName());
            result.put("prizeImageUrl", cell.getPrizeImageUrl());
            result.put("shards", mockShards);
            results.add(result);
        }

        boolean mockBingo = count >= 3 && random.nextInt(5) == 0;

        Map<String, Object> response = new HashMap<>();
        response.put("isTrial", true);
        response.put("gameName", game.getName());
        response.put("pricePerDig", game.getPricePerDig());
        response.put("gridSize", game.getGridSize());
        response.put("results", results);
        response.put("hasBingo", mockBingo);
        if (mockBingo) {
            response.put("bingoRewardName", game.getBingoRewardName());
        }
        response.put("message", "這是試挖結果，正式遊戲需要登入並使用代幣");

        String message = mockBingo ? "🎉 試挖連線成功！體驗中獎的快感！" : "試挖完成！";
        return ApiResponse.ok(response, message);
    }
}
