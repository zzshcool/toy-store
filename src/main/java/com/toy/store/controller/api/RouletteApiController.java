package com.toy.store.controller.api;

import com.toy.store.exception.AppException;

import com.toy.store.annotation.CurrentUser;
import com.toy.store.dto.ApiResponse;
import com.toy.store.model.*;
import com.toy.store.service.RouletteService;
import com.toy.store.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 轉盤 API
 */
@RestController
@RequestMapping("/api/roulette")
public class RouletteApiController {

    @Autowired
    private RouletteService rouletteService;

    @Autowired
    private com.toy.store.repository.MemberRepository memberRepository;

    /**
     * 取得所有進行中的轉盤
     */
    @GetMapping
    public ApiResponse<List<Map<String, Object>>> getActiveGames() {
        List<RouletteGame> games = rouletteService.getActiveGames();
        List<Map<String, Object>> result = games.stream().map(this::gameToMap).collect(Collectors.toList());
        return ApiResponse.ok(result);
    }

    /**
     * 取得單一轉盤詳情（含獎格）
     */
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getGame(@PathVariable Long id) {
        RouletteGame game = rouletteService.getGameWithSlots(id);
        if (game == null) {
            return ApiResponse.error("轉盤不存在");
        }
        Map<String, Object> result = gameToMap(game);
        result.put("slots", rouletteService.getSlots(id).stream()
                .map(this::slotToMap).collect(Collectors.toList()));
        return ApiResponse.ok(result);
    }

    /**
     * 旋轉轉盤
     */
    @PostMapping("/{id}/spin")
    public ApiResponse<Map<String, Object>> spin(
            @PathVariable Long id,
            @CurrentUser TokenService.TokenInfo info) {

        Long memberId = getMemberId(info);
        if (memberId == null) {
            throw new AppException("請先登入");
        }

        RouletteService.SpinResult result = rouletteService.spin(id, memberId);
        Map<String, Object> response = new HashMap<>();
        response.put("slot", slotToMap(result.getSlot()));
        response.put("isGuarantee", result.isGuarantee());
        response.put("isFreeSpin", result.isFreeSpin());
        response.put("shardsEarned", result.getShardsEarned());
        response.put("currentLuckyValue", result.getCurrentLuckyValue());
        response.put("luckyThreshold", result.getLuckyThreshold());
        response.put("luckyPercentage", result.getLuckyPercentage());

        String message = result.isGuarantee() ? "✨ 保底觸發！獲得大獎！" : "旋轉完成！";
        return ApiResponse.ok(response, message);
    }

    /**
     * 取得會員幸運值
     */
    @GetMapping("/lucky-value")
    public ApiResponse<Map<String, Object>> getLuckyValue(@CurrentUser TokenService.TokenInfo info) {
        Long memberId = getMemberId(info);
        if (memberId == null) {
            return ApiResponse.error("請先登入");
        }

        MemberLuckyValue luckyValue = rouletteService.getMemberLuckyValue(memberId);
        Map<String, Object> result = new HashMap<>();
        result.put("luckyValue", luckyValue.getLuckyValue());
        result.put("shardBalance", luckyValue.getShardBalance());
        return ApiResponse.ok(result);
    }

    private Long getMemberId(TokenService.TokenInfo info) {
        if (info == null)
            return null;
        return memberRepository.findByUsername(info.getUsername())
                .map(Member::getId)
                .orElse(null);
    }

    private Map<String, Object> gameToMap(RouletteGame game) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", game.getId());
        map.put("name", game.getName());
        map.put("description", game.getDescription());
        map.put("imageUrl", game.getImageUrl());
        map.put("pricePerSpin", game.getPricePerSpin());
        map.put("totalSlots", game.getTotalSlots());
        map.put("slotAngle", game.getSlotAngle());
        map.put("ipName", game.getIpName());
        return map;
    }

    private Map<String, Object> slotToMap(RouletteSlot slot) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", slot.getId());
        map.put("slotOrder", slot.getSlotOrder());
        map.put("slotType", slot.getSlotType().name());
        map.put("slotTypeDisplay", slot.getSlotType().getDisplayName());
        map.put("prizeName", slot.getPrizeName());
        map.put("color", slot.getColor());
        map.put("isJackpot", slot.isJackpot());
        return map;
    }
}
