package com.toy.store.controller.api;

import com.toy.store.annotation.CurrentUser;
import com.toy.store.exception.AppException;
import com.toy.store.dto.ApiResponse;
import com.toy.store.model.*;
import com.toy.store.service.IchibanService;
import com.toy.store.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 一番賞 API
 */
@RestController
@RequestMapping("/api/ichiban")
public class IchibanApiController {

    @Autowired
    private IchibanService ichibanService;

    @Autowired
    private com.toy.store.repository.MemberRepository memberRepository;

    /**
     * 取得所有進行中的一番賞
     */
    @GetMapping
    public ApiResponse<List<Map<String, Object>>> getActiveBoxes() {
        List<IchibanBox> boxes = ichibanService.getActiveBoxes();
        List<Map<String, Object>> result = boxes.stream().map(this::boxToMap).collect(Collectors.toList());
        return ApiResponse.ok(result);
    }

    /**
     * 取得單一箱體詳情
     */
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getBox(@PathVariable Long id) {
        IchibanBox box = ichibanService.getBoxWithSlots(id);
        if (box == null) {
            return ApiResponse.error("箱體不存在");
        }
        return ApiResponse.ok(boxToMap(box));
    }

    /**
     * 取得格子狀態
     */
    @GetMapping("/{id}/slots")
    public ApiResponse<List<Map<String, Object>>> getSlots(@PathVariable Long id) {
        List<IchibanSlot> slots = ichibanService.getSlots(id);
        List<Map<String, Object>> result = slots.stream().map(this::slotToMap).collect(Collectors.toList());
        return ApiResponse.ok(result);
    }

    /**
     * 鎖定格子
     */
    @PostMapping("/{id}/slots/{num}/lock")
    public ApiResponse<Map<String, Object>> lockSlot(
            @PathVariable Long id,
            @PathVariable Integer num,
            @CurrentUser TokenService.TokenInfo info) {

        Long memberId = getMemberId(info);
        if (memberId == null) {
            throw new AppException("請先登入");
        }

        IchibanSlot slot = ichibanService.lockSlot(id, num, memberId);
        return ApiResponse.ok(slotToMap(slot), "格子已鎖定，請在3分鐘內完成付款");
    }

    /**
     * 揭曉格子（付款後）
     */
    @PostMapping("/{id}/slots/{num}/reveal")
    public ApiResponse<Map<String, Object>> revealSlot(
            @PathVariable Long id,
            @PathVariable Integer num,
            @CurrentUser TokenService.TokenInfo info) {

        Long memberId = getMemberId(info);
        if (memberId == null) {
            throw new AppException("請先登入");
        }

        IchibanSlot slot = ichibanService.revealSlot(id, num, memberId);
        Map<String, Object> result = slotToMap(slot);
        if (slot.getPrize() != null) {
            result.put("prize", prizeToMap(slot.getPrize()));
        }
        return ApiResponse.ok(result, "恭喜獲得獎品！");
    }

    /**
     * 多格購買（推薦使用此 API）
     * Request Body: { "slotNumbers": [1, 3, 5, 7] }
     */
    @PostMapping("/{id}/purchase")
    public ApiResponse<Map<String, Object>> purchaseSlots(
            @PathVariable Long id,
            @RequestBody Map<String, List<Integer>> body,
            @CurrentUser TokenService.TokenInfo info) {

        Long memberId = getMemberId(info);
        if (memberId == null) {
            throw new AppException("請先登入");
        }

        List<Integer> slotNumbers = body.get("slotNumbers");
        if (slotNumbers == null || slotNumbers.isEmpty()) {
            throw new AppException("請選擇至少一個格子");
        }

        IchibanService.PurchaseResult result = ichibanService.purchaseMultipleSlots(id, slotNumbers, memberId);

        Map<String, Object> response = new HashMap<>();
        response.put("totalCost", result.getTotalCost());
        response.put("totalShards", result.getTotalShards());
        response.put("prizes", result.getSlots().stream().map(sr -> {
            Map<String, Object> m = new HashMap<>();
            m.put("slotNumber", sr.getSlotNumber());
            m.put("shards", sr.getShards());
            if (sr.getPrize() != null) {
                m.put("prize", prizeToMap(sr.getPrize()));
            }
            return m;
        }).collect(Collectors.toList()));

        return ApiResponse.ok(response, "購買成功！共抽取 " + slotNumbers.size() + " 格");
    }

    private Long getMemberId(TokenService.TokenInfo info) {
        if (info == null)
            return null;
        return memberRepository.findByUsername(info.getUsername())
                .map(Member::getId)
                .orElse(null);
    }

    private Map<String, Object> boxToMap(IchibanBox box) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", box.getId());
        map.put("name", box.getName());
        map.put("description", box.getDescription());
        map.put("imageUrl", box.getImageUrl());
        map.put("pricePerDraw", box.getPricePerDraw());
        map.put("totalSlots", box.getTotalSlots());
        map.put("status", box.getStatus().name());
        map.put("ipName", box.getIpName());

        if (box.getPrizes() != null) {
            map.put("prizes", box.getPrizes().stream().map(this::prizeToMap).collect(Collectors.toList()));
        }

        // 計算剩餘格子
        int remainingSlots = (int) box.getSlots().stream().filter(s -> s.getStatus() == IchibanSlot.Status.AVAILABLE)
                .count();
        map.put("remainingSlots", remainingSlots);

        return map;
    }

    private Map<String, Object> slotToMap(IchibanSlot slot) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", slot.getId());
        map.put("slotNumber", slot.getSlotNumber());
        map.put("formattedNumber", slot.getFormattedNumber());
        map.put("status", slot.getStatus().name());
        map.put("isLockExpired", slot.isLockExpired());
        return map;
    }

    private Map<String, Object> prizeToMap(IchibanPrize prize) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", prize.getId());
        map.put("name", prize.getName());
        map.put("rank", prize.getRank().name());
        map.put("rankDisplay", prize.getRank().getDisplayName());
        map.put("imageUrl", prize.getImageUrl());
        map.put("estimatedValue", prize.getEstimatedValue());
        map.put("totalQuantity", prize.getTotalQuantity());
        map.put("remainingQuantity", prize.getRemainingQuantity());
        return map;
    }

    // ============== 試抽功能 (無需登入，不扣代幣) ==============

    /**
     * 試抽 - 模擬一番賞抽獎體驗
     * 不需登入，不扣代幣，隨機返回獎品結果
     * 
     * @param id    箱體 ID
     * @param count 模擬抽取數量 (1-10)
     */
    @PostMapping("/{id}/trial")
    public ApiResponse<Map<String, Object>> trial(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Integer> body) {

        IchibanBox box = ichibanService.getBoxWithSlots(id);
        if (box == null) {
            return ApiResponse.error("箱體不存在");
        }

        int count = 1;
        if (body != null && body.containsKey("count")) {
            count = Math.max(1, Math.min(10, body.get("count")));
        }

        // 從獎品池中隨機抽取（模擬）
        java.util.List<Map<String, Object>> results = new java.util.ArrayList<>();
        java.util.Random random = new java.util.Random();

        // 獲取所有可用獎品（有剩餘數量的）
        java.util.List<IchibanPrize> availablePrizes = box.getPrizes().stream()
                .filter(p -> p.getRemainingQuantity() > 0)
                .collect(Collectors.toList());

        if (availablePrizes.isEmpty()) {
            return ApiResponse.error("此箱體已售罄，無法試抽");
        }

        // 計算總權重（依剩餘數量加權）
        int totalWeight = availablePrizes.stream()
                .mapToInt(IchibanPrize::getRemainingQuantity)
                .sum();

        for (int i = 0; i < count; i++) {
            // 隨機抽取
            int roll = random.nextInt(totalWeight);
            int cumulative = 0;
            IchibanPrize selectedPrize = availablePrizes.get(0);

            for (IchibanPrize prize : availablePrizes) {
                cumulative += prize.getRemainingQuantity();
                if (roll < cumulative) {
                    selectedPrize = prize;
                    break;
                }
            }

            // 模擬格子編號
            int mockSlotNumber = random.nextInt(box.getTotalSlots()) + 1;
            int mockShards = random.nextInt(20) + 1;

            Map<String, Object> result = new HashMap<>();
            result.put("slotNumber", String.format("%02d", mockSlotNumber));
            result.put("shards", mockShards);
            result.put("prize", prizeToMap(selectedPrize));
            results.add(result);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("isTrial", true);
        response.put("boxName", box.getName());
        response.put("pricePerDraw", box.getPricePerDraw());
        response.put("results", results);
        response.put("message", "這是試抽結果，正式抽獎需要登入並使用代幣");

        return ApiResponse.ok(response, "試抽完成！體驗抽獎的樂趣～");
    }
}
