package com.toy.store.controller.api;

import com.toy.store.annotation.CurrentUser;
import com.toy.store.dto.ApiResponse;
import com.toy.store.exception.AppException;
import com.toy.store.model.*;
import com.toy.store.service.BlindBoxService;
import com.toy.store.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 盲盒（動漫周邊）API
 * 對應規格書 §4.D 動漫周邊系統
 */
@RestController
@RequestMapping("/api/blindbox")
public class BlindBoxApiController {

    @Autowired
    private BlindBoxService blindBoxService;

    @Autowired
    private com.toy.store.repository.MemberRepository memberRepository;

    /**
     * 取得所有進行中的盲盒
     */
    @GetMapping
    public ApiResponse<List<Map<String, Object>>> getActiveBoxes() {
        List<BlindBox> boxes = blindBoxService.getActiveBoxes();
        List<Map<String, Object>> result = boxes.stream().map(this::boxToMap).collect(Collectors.toList());
        return ApiResponse.ok(result);
    }

    /**
     * 取得單一盲盒詳情（含所有單品狀態）
     */
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getBox(@PathVariable Long id) {
        BlindBox box = blindBoxService.getBoxWithItems(id);
        if (box == null) {
            return ApiResponse.error("盲盒不存在");
        }
        Map<String, Object> result = boxToMap(box);
        result.put("items", blindBoxService.getItems(id).stream()
                .map(this::itemToMap).collect(Collectors.toList()));
        return ApiResponse.ok(result);
    }

    /**
     * 鎖定盒子（開始 180 秒倒數）
     */
    @PostMapping("/{id}/items/{num}/lock")
    public ApiResponse<Map<String, Object>> lockItem(
            @PathVariable Long id,
            @PathVariable Integer num,
            @CurrentUser TokenService.TokenInfo info) {

        Long memberId = getMemberId(info);
        if (memberId == null) {
            throw new AppException("請先登入");
        }

        BlindBoxItem item = blindBoxService.lockItem(id, num, memberId);
        Map<String, Object> result = itemToMap(item);
        result.put("remainingSeconds", item.getRemainingLockSeconds());
        return ApiResponse.ok(result, "盒子已鎖定，請在 180 秒內決定是否購買");
    }

    /**
     * 確認購買
     */
    @PostMapping("/{id}/items/{num}/purchase")
    public ApiResponse<Map<String, Object>> purchaseItem(
            @PathVariable Long id,
            @PathVariable Integer num,
            @CurrentUser TokenService.TokenInfo info) {

        Long memberId = getMemberId(info);
        if (memberId == null) {
            throw new AppException("請先登入");
        }

        BlindBoxService.PurchaseResult result = blindBoxService.purchaseItem(id, num, memberId);

        Map<String, Object> response = new HashMap<>();
        response.put("item", itemToMap(result.getItem()));
        response.put("cost", result.getCost());
        response.put("shardsEarned", result.getShards());

        String rarityEmoji = getRarityEmoji(result.getItem().getRarity());
        return ApiResponse.ok(response, rarityEmoji + " 恭喜獲得：" + result.getItem().getPrizeName());
    }

    /**
     * 全包購買（整中盒）
     */
    @PostMapping("/{id}/full-purchase")
    public ApiResponse<Map<String, Object>> purchaseFullBox(
            @PathVariable Long id,
            @CurrentUser TokenService.TokenInfo info) {

        Long memberId = getMemberId(info);
        if (memberId == null) {
            throw new AppException("請先登入");
        }

        BlindBoxService.FullBoxResult result = blindBoxService.purchaseFullBox(id, memberId);

        Map<String, Object> response = new HashMap<>();
        response.put("items", result.getItems().stream().map(this::itemToMap).collect(Collectors.toList()));
        response.put("totalCost", result.getCost());
        response.put("totalShards", result.getTotalShards());
        response.put("itemCount", result.getItems().size());

        return ApiResponse.ok(response, "🎉 全包成功！共獲得 " + result.getItems().size() + " 件商品！");
    }

    /**
     * 天選抽（電腦隨機選號並購買）
     */
    @PostMapping("/{id}/random-purchase")
    public ApiResponse<Map<String, Object>> randomPurchase(
            @PathVariable Long id,
            @CurrentUser TokenService.TokenInfo info) {

        Long memberId = getMemberId(info);
        if (memberId == null) {
            throw new AppException("請先登入");
        }

        BlindBoxService.PurchaseResult result = blindBoxService.randomPurchase(id, memberId);

        Map<String, Object> response = new HashMap<>();
        response.put("item", itemToMap(result.getItem()));
        response.put("cost", result.getCost());
        response.put("shardsEarned", result.getShards());

        return ApiResponse.ok(response, "✨ 天選之人！獲得：" + result.getItem().getPrizeName());
    }

    /**
     * 使用提示卡
     */
    @PostMapping("/{id}/use-hint")
    public ApiResponse<List<Map<String, Object>>> useHintCard(
            @PathVariable Long id,
            @CurrentUser TokenService.TokenInfo info) {

        Long memberId = getMemberId(info);
        if (memberId == null) {
            throw new AppException("請先登入");
        }

        List<BlindBoxItem> filteredItems = blindBoxService.useHintCard(id, memberId);
        List<Map<String, Object>> result = filteredItems.stream()
                .map(this::itemToMap).collect(Collectors.toList());

        return ApiResponse.ok(result, "💡 提示卡已使用！已排除部分選項");
    }

    /**
     * 使用透視卡
     */
    @PostMapping("/{id}/items/{num}/use-peek")
    public ApiResponse<Map<String, Object>> usePeekCard(
            @PathVariable Long id,
            @PathVariable Integer num,
            @CurrentUser TokenService.TokenInfo info) {

        Long memberId = getMemberId(info);
        if (memberId == null) {
            throw new AppException("請先登入");
        }

        BlindBoxItem item = blindBoxService.usePeekCard(id, num, memberId);
        Map<String, Object> result = itemToMap(item);
        // 透視卡顯示完整內容
        result.put("prizeName", item.getPrizeName());
        result.put("prizeDescription", item.getPrizeDescription());
        result.put("prizeImageUrl", item.getPrizeImageUrl());
        result.put("rarity", item.getRarity().name());
        result.put("rarityDisplay", item.getRarity().getDisplayName());

        return ApiResponse.ok(result, "👁️ 透視卡已使用！這盒含有：" + item.getPrizeName());
    }

    /**
     * 使用換一盒
     */
    @PostMapping("/{id}/items/{num}/use-swap")
    public ApiResponse<Map<String, Object>> useSwapCard(
            @PathVariable Long id,
            @PathVariable Integer num,
            @CurrentUser TokenService.TokenInfo info) {

        Long memberId = getMemberId(info);
        if (memberId == null) {
            throw new AppException("請先登入");
        }

        BlindBoxItem newItem = blindBoxService.useSwapCard(id, num, memberId);
        Map<String, Object> result = itemToMap(newItem);
        result.put("remainingSeconds", newItem.getRemainingLockSeconds());

        return ApiResponse.ok(result, "🔄 已換到新盒子 #" + newItem.getBoxNumber() + "！");
    }

    /**
     * 試抽
     */
    @PostMapping("/{id}/trial")
    public ApiResponse<Map<String, Object>> trial(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Integer> body) {

        int count = 1;
        if (body != null && body.containsKey("count")) {
            count = Math.max(1, Math.min(12, body.get("count")));
        }

        BlindBoxService.TrialResult result = blindBoxService.trial(id, count);

        Map<String, Object> response = new HashMap<>();
        response.put("isTrial", true);
        response.put("boxName", result.getBox().getName());
        response.put("pricePerBox", result.getBox().getPricePerBox());
        response.put("results", result.getResults().stream().map(item -> {
            Map<String, Object> m = new HashMap<>();
            m.put("boxNumber", item.getBoxNumber());
            m.put("prizeName", item.getPrizeName());
            m.put("rarity", item.getRarity().name());
            m.put("rarityDisplay", item.getRarity().getDisplayName());
            m.put("prizeImageUrl", item.getPrizeImageUrl());
            m.put("shards", 10 + new java.util.Random().nextInt(40));
            return m;
        }).collect(Collectors.toList()));
        response.put("message", "這是試抽結果，正式購買需要登入並使用代幣");

        return ApiResponse.ok(response, "試抽完成！體驗盲盒的樂趣～");
    }

    private Long getMemberId(TokenService.TokenInfo info) {
        if (info == null)
            return null;
        return memberRepository.findByUsername(info.getUsername())
                .map(Member::getId)
                .orElse(null);
    }

    private Map<String, Object> boxToMap(BlindBox box) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", box.getId());
        map.put("name", box.getName());
        map.put("description", box.getDescription());
        map.put("imageUrl", box.getImageUrl());
        map.put("ipName", box.getIpName());
        map.put("pricePerBox", box.getPricePerBox());
        map.put("fullBoxPrice", box.getFullBoxPrice());
        map.put("totalBoxes", box.getTotalBoxes());
        map.put("remainingCount", box.getRemainingCount());
        map.put("status", box.getStatus().name());
        return map;
    }

    private Map<String, Object> itemToMap(BlindBoxItem item) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", item.getId());
        map.put("boxNumber", item.getBoxNumber());
        map.put("status", item.getStatus().name());
        map.put("isLockExpired", item.isLockExpired());

        // 只有已售出的才顯示內容
        if (item.getStatus() == BlindBoxItem.Status.SOLD) {
            map.put("prizeName", item.getPrizeName());
            map.put("prizeImageUrl", item.getPrizeImageUrl());
            map.put("rarity", item.getRarity().name());
            map.put("rarityDisplay", item.getRarity().getDisplayName());
        }

        return map;
    }

    private String getRarityEmoji(BlindBoxItem.Rarity rarity) {
        return switch (rarity) {
            case SECRET -> "🌟";
            case ULTRA_RARE -> "💎";
            case RARE -> "✨";
            case NORMAL -> "🎁";
        };
    }
}
