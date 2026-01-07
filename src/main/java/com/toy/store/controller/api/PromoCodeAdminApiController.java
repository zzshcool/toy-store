package com.toy.store.controller.api;

import com.toy.store.dto.ApiResponse;
import com.toy.store.model.PromoCode;
import com.toy.store.model.PromoCodeUsage;
import com.toy.store.mapper.PromoCodeMapper;
import com.toy.store.mapper.PromoCodeUsageMapper;
import com.toy.store.service.PromoCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 禮包碼後台管理 API
 */
@RestController
@RequestMapping("/api/admin/promo-codes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasAuthority('GACHA_MANAGE')")
public class PromoCodeAdminApiController {

    private final PromoCodeService promoCodeService;
    private final PromoCodeMapper promoCodeMapper;
    private final PromoCodeUsageMapper usageMapper;

    /**
     * 取得所有禮包碼
     */
    @GetMapping
    public ApiResponse<List<Map<String, Object>>> getAllPromoCodes() {
        List<PromoCode> codes = promoCodeMapper.findAll();
        List<Map<String, Object>> result = codes.stream()
                .map(this::mapPromoCode)
                .collect(Collectors.toList());
        return ApiResponse.ok(result);
    }

    /**
     * 建立禮包碼
     */
    @PostMapping
    public ApiResponse<Map<String, Object>> createPromoCode(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        String description = (String) request.get("description");
        String rewardTypeStr = (String) request.get("rewardType");
        Number rewardValueNum = (Number) request.get("rewardValue");
        Number maxUsesNum = (Number) request.get("maxUses");
        String validUntilStr = (String) request.get("validUntil");

        if (name == null || rewardTypeStr == null || rewardValueNum == null) {
            return ApiResponse.error("缺少必要欄位");
        }

        PromoCode.RewardType rewardType;
        try {
            rewardType = PromoCode.RewardType.valueOf(rewardTypeStr);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("無效的獎勵類型");
        }

        BigDecimal rewardValue = BigDecimal.valueOf(rewardValueNum.doubleValue());
        int maxUses = maxUsesNum != null ? maxUsesNum.intValue() : 100;
        LocalDateTime validUntil = null;
        if (validUntilStr != null && !validUntilStr.isEmpty()) {
            validUntil = LocalDateTime.parse(validUntilStr);
        }

        PromoCode code = promoCodeService.createGiftCode(name, description, rewardType, rewardValue, maxUses,
                validUntil);
        return ApiResponse.ok(mapPromoCode(code));
    }

    /**
     * 刪除禮包碼
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePromoCode(@PathVariable Long id) {
        if (promoCodeMapper.findById(id).isEmpty()) {
            return ApiResponse.error("禮包碼不存在");
        }
        promoCodeMapper.deleteById(id);
        return ApiResponse.ok(null);
    }

    /**
     * 切換啟用狀態
     */
    @PostMapping("/{id}/toggle")
    public ApiResponse<Map<String, Object>> togglePromoCode(@PathVariable Long id) {
        PromoCode code = promoCodeMapper.findById(id).orElse(null);
        if (code == null) {
            return ApiResponse.error("禮包碼不存在");
        }
        code.setEnabled(!code.getEnabled());
        promoCodeMapper.update(code);
        return ApiResponse.ok(mapPromoCode(code));
    }

    /**
     * 取得兌換記錄
     */
    @GetMapping("/{id}/usages")
    public ApiResponse<List<Map<String, Object>>> getUsages(@PathVariable Long id) {
        List<PromoCodeUsage> usages = usageMapper.findByPromoCodeId(id);
        List<Map<String, Object>> result = usages.stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("memberId", u.getMemberId());
            map.put("usedAt", u.getUsedAt() != null ? u.getUsedAt().toString() : null);
            return map;
        }).collect(Collectors.toList());
        return ApiResponse.ok(result);
    }

    private Map<String, Object> mapPromoCode(PromoCode code) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", code.getId());
        map.put("code", code.getCode());
        map.put("name", code.getName());
        map.put("description", code.getDescription());
        map.put("type", code.getCodeType() != null ? code.getCodeType().name() : null);
        map.put("rewardType", code.getRewardTypeEnum() != null ? code.getRewardTypeEnum().name() : null);
        map.put("rewardValue", code.getRewardValue());
        map.put("maxUses", code.getMaxUses());
        map.put("usedCount", code.getUsedCount());
        map.put("enabled", code.getEnabled());
        map.put("validUntil", code.getValidUntil() != null ? code.getValidUntil().toString() : null);
        map.put("createdAt", code.getCreatedAt() != null ? code.getCreatedAt().toString() : null);
        return map;
    }
}
