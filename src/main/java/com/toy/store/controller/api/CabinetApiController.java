package com.toy.store.controller.api;

import com.toy.store.annotation.CurrentUser;
import com.toy.store.dto.ApiResponse;
import com.toy.store.exception.AppException;
import com.toy.store.model.*;
import com.toy.store.service.CabinetService;
import com.toy.store.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 盒櫃 API
 * 對應規格書 §4.D, §8.A
 */
@RestController
@RequestMapping("/api/cabinet")
public class CabinetApiController {

    @Autowired
    private CabinetService cabinetService;

    @Autowired
    private com.toy.store.repository.MemberRepository memberRepository;

    /**
     * 取得盒櫃內的獎品
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> getCabinet(@CurrentUser TokenService.TokenInfo info) {
        Long memberId = getMemberId(info);
        if (memberId == null) {
            throw new AppException("請先登入");
        }

        List<CabinetItem> items = cabinetService.getCabinetItems(memberId);
        CabinetService.ShippingInfo shippingInfo = cabinetService.calculateShipping(items.size());

        Map<String, Object> result = new HashMap<>();
        result.put("items", items.stream().map(this::itemToMap).collect(Collectors.toList()));
        result.put("totalCount", items.size());
        result.put("isFreeShipping", shippingInfo.isFreeShipping());
        result.put("shippingFee", shippingInfo.getFee());
        result.put("itemsNeededForFree", shippingInfo.getItemsNeededForFree());

        return ApiResponse.ok(result);
    }

    /**
     * 取得所有獎品紀錄（含已發貨）
     */
    @GetMapping("/all")
    public ApiResponse<List<Map<String, Object>>> getAllItems(@CurrentUser TokenService.TokenInfo info) {
        Long memberId = getMemberId(info);
        if (memberId == null) {
            throw new AppException("請先登入");
        }

        List<CabinetItem> items = cabinetService.getAllItems(memberId);
        return ApiResponse.ok(items.stream().map(this::itemToMap).collect(Collectors.toList()));
    }

    /**
     * 計算運費預覽
     */
    @PostMapping("/shipping-preview")
    public ApiResponse<Map<String, Object>> shippingPreview(
            @RequestBody Map<String, List<Long>> body,
            @CurrentUser TokenService.TokenInfo info) {

        Long memberId = getMemberId(info);
        if (memberId == null) {
            throw new AppException("請先登入");
        }

        List<Long> itemIds = body.get("itemIds");
        if (itemIds == null || itemIds.isEmpty()) {
            throw new AppException("請選擇要發貨的獎品");
        }

        CabinetService.ShippingInfo shippingInfo = cabinetService.calculateShipping(itemIds.size());

        Map<String, Object> result = new HashMap<>();
        result.put("itemCount", itemIds.size());
        result.put("isFreeShipping", shippingInfo.isFreeShipping());
        result.put("shippingFee", shippingInfo.getFee());
        result.put("itemsNeededForFree", shippingInfo.getItemsNeededForFree());

        return ApiResponse.ok(result);
    }

    /**
     * 提交發貨申請
     */
    @PostMapping("/ship")
    public ApiResponse<Map<String, Object>> requestShipment(
            @RequestBody ShipmentRequestDTO body,
            @CurrentUser TokenService.TokenInfo info) {

        Long memberId = getMemberId(info);
        if (memberId == null) {
            throw new AppException("請先登入");
        }

        ShipmentRequest request = cabinetService.requestShipment(
                memberId,
                body.getItemIds(),
                body.getRecipientName(),
                body.getRecipientPhone(),
                body.getRecipientAddress(),
                body.getPostalCode());

        Map<String, Object> result = shipmentToMap(request);
        String message = request.getIsFreeShipping()
                ? "📦 發貨申請已提交（滿5件免運）"
                : "📦 發貨申請已提交";

        return ApiResponse.ok(result, message);
    }

    /**
     * 取得發貨申請列表
     */
    @GetMapping("/shipments")
    public ApiResponse<List<Map<String, Object>>> getShipments(@CurrentUser TokenService.TokenInfo info) {
        Long memberId = getMemberId(info);
        if (memberId == null) {
            throw new AppException("請先登入");
        }

        List<ShipmentRequest> shipments = cabinetService.getMemberShipments(memberId);
        return ApiResponse.ok(shipments.stream().map(this::shipmentToMap).collect(Collectors.toList()));
    }

    /**
     * 取消發貨申請
     */
    @PostMapping("/shipments/{id}/cancel")
    public ApiResponse<Void> cancelShipment(
            @PathVariable Long id,
            @CurrentUser TokenService.TokenInfo info) {

        Long memberId = getMemberId(info);
        if (memberId == null) {
            throw new AppException("請先登入");
        }

        cabinetService.cancelShipment(memberId, id);
        return ApiResponse.ok(null, "發貨申請已取消");
    }

    /**
     * 兌換獎品為積分
     */
    @PostMapping("/items/{id}/exchange")
    public ApiResponse<Map<String, Object>> exchangeForPoints(
            @PathVariable Long id,
            @CurrentUser TokenService.TokenInfo info) {

        Long memberId = getMemberId(info);
        if (memberId == null) {
            throw new AppException("請先登入");
        }

        int points = cabinetService.exchangeForPoints(memberId, id);

        Map<String, Object> result = new HashMap<>();
        result.put("pointsEarned", points);

        return ApiResponse.ok(result, "🎉 兌換成功！獲得 " + points + " 積分");
    }

    private Long getMemberId(TokenService.TokenInfo info) {
        if (info == null)
            return null;
        return memberRepository.findByUsername(info.getUsername())
                .map(Member::getId)
                .orElse(null);
    }

    private Map<String, Object> itemToMap(CabinetItem item) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", item.getId());
        map.put("prizeName", item.getPrizeName());
        map.put("prizeDescription", item.getPrizeDescription());
        map.put("prizeImageUrl", item.getPrizeImageUrl());
        map.put("prizeRank", item.getPrizeRank());
        map.put("sourceType", item.getSourceType().name());
        map.put("sourceTypeDisplay", item.getSourceType().getDisplayName());
        map.put("status", item.getStatus().name());
        map.put("obtainedAt", item.getObtainedAt());
        return map;
    }

    private Map<String, Object> shipmentToMap(ShipmentRequest request) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", request.getId());
        map.put("recipientName", request.getRecipientName());
        map.put("recipientAddress", request.getRecipientAddress());
        map.put("itemCount", request.getItemCount());
        map.put("isFreeShipping", request.getIsFreeShipping());
        map.put("shippingFee", request.getShippingFee());
        map.put("status", request.getStatus().name());
        map.put("statusDisplay", request.getStatus().getDisplayName());
        map.put("trackingNumber", request.getTrackingNumber());
        map.put("shippingCompany", request.getShippingCompany());
        map.put("createdAt", request.getCreatedAt());
        map.put("shippedAt", request.getShippedAt());
        return map;
    }

    @lombok.Data
    public static class ShipmentRequestDTO {
        private List<Long> itemIds;
        private String recipientName;
        private String recipientPhone;
        private String recipientAddress;
        private String postalCode;
    }
}
