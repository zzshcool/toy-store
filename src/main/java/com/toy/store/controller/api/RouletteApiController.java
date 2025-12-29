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

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AppException("會員不存在"));
        Map<String, Object> result = new HashMap<>();
        result.put("luckyValue", member.getLuckyValue());
        result.put("shardBalance", member.getPoints());
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

    // ============== 試抽功能 (無需登入，不扣代幣) ==============

    /**
     * 試轉 - 模擬轉盤體驗
     * 不需登入，不扣代幣，隨機返回結果
     */
    @PostMapping("/{id}/trial")
    public ApiResponse<Map<String, Object>> trial(@PathVariable Long id) {
        RouletteGame game = rouletteService.getGameWithSlots(id);
        if (game == null) {
            return ApiResponse.error("轉盤不存在");
        }

        List<RouletteSlot> slots = rouletteService.getSlots(id);
        if (slots.isEmpty()) {
            return ApiResponse.error("轉盤尚未設定獎格");
        }

        // 隨機選擇一個獎格（依權重）
        java.util.Random random = new java.util.Random();
        int totalWeight = slots.stream().mapToInt(RouletteSlot::getWeight).sum();
        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        RouletteSlot selectedSlot = slots.get(0);

        for (RouletteSlot slot : slots) {
            cumulative += slot.getWeight();
            if (roll < cumulative) {
                selectedSlot = slot;
                break;
            }
        }

        int mockShards = random.nextInt(20) + 1;
        boolean isMockFreeSpin = selectedSlot.getSlotType() == RouletteSlot.SlotType.FREE_SPIN;

        Map<String, Object> response = new HashMap<>();
        response.put("isTrial", true);
        response.put("gameName", game.getName());
        response.put("pricePerSpin", game.getPricePerSpin());
        response.put("slot", slotToMap(selectedSlot));
        response.put("shardsEarned", mockShards);
        response.put("isFreeSpin", isMockFreeSpin);
        response.put("isGuarantee", false);
        response.put("currentLuckyValue", 0);
        response.put("luckyThreshold", 100);
        response.put("luckyPercentage", 0);
        response.put("message", "這是試轉結果，正式抽獎需要登入並使用代幣");

        String message = selectedSlot.isJackpot() ? "🎉 試轉中獎！體驗大獎的感覺～" : "試轉完成！";
        return ApiResponse.ok(response, message);
    }
}
