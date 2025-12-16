package com.toy.store.controller.api;

import com.toy.store.dto.ApiResponse;
import com.toy.store.model.*;
import com.toy.store.service.IchibanService;
import com.toy.store.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
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
            HttpServletRequest request) {

        Long memberId = getMemberId(request);
        if (memberId == null) {
            return ApiResponse.error("請先登入");
        }

        try {
            IchibanSlot slot = ichibanService.lockSlot(id, num, memberId);
            return ApiResponse.ok(slotToMap(slot), "格子已鎖定，請在3分鐘內完成付款");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 揭曉格子（付款後）
     */
    @PostMapping("/{id}/slots/{num}/reveal")
    public ApiResponse<Map<String, Object>> revealSlot(
            @PathVariable Long id,
            @PathVariable Integer num,
            HttpServletRequest request) {

        Long memberId = getMemberId(request);
        if (memberId == null) {
            return ApiResponse.error("請先登入");
        }

        try {
            IchibanSlot slot = ichibanService.revealSlot(id, num, memberId);
            Map<String, Object> result = slotToMap(slot);
            if (slot.getPrize() != null) {
                result.put("prize", prizeToMap(slot.getPrize()));
            }
            return ApiResponse.ok(result, "恭喜獲得獎品！");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 多格購買（推薦使用此 API）
     * Request Body: { "slotNumbers": [1, 3, 5, 7] }
     */
    @PostMapping("/{id}/purchase")
    public ApiResponse<Map<String, Object>> purchaseSlots(
            @PathVariable Long id,
            @RequestBody Map<String, List<Integer>> body,
            HttpServletRequest request) {

        Long memberId = getMemberId(request);
        if (memberId == null) {
            return ApiResponse.error("請先登入");
        }

        List<Integer> slotNumbers = body.get("slotNumbers");
        if (slotNumbers == null || slotNumbers.isEmpty()) {
            return ApiResponse.error("請選擇至少一個格子");
        }

        try {
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
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    private Long getMemberId(HttpServletRequest request) {
        TokenService.TokenInfo info = (TokenService.TokenInfo) request.getAttribute("currentUser");
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
        return map;
    }
}
