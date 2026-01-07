package com.toy.store.controller.api;

import com.toy.store.dto.ApiResponse;
import com.toy.store.model.Coupon;
import com.toy.store.mapper.CouponMapper;
import com.toy.store.service.CouponService;
import com.toy.store.service.MemberTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 優惠券後台管理 API
 */
@RestController
@RequestMapping("/api/admin/coupons")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasAuthority('GACHA_MANAGE')")
public class CouponAdminApiController {

    private final CouponService couponService;
    private final CouponMapper couponMapper;
    private final MemberTagService tagService;

    /**
     * 取得所有優惠券
     */
    @GetMapping
    public ApiResponse<List<Map<String, Object>>> getAllCoupons() {
        List<Coupon> coupons = couponMapper.findAll();
        List<Map<String, Object>> result = coupons.stream()
                .map(this::mapCoupon)
                .collect(Collectors.toList());
        return ApiResponse.ok(result);
    }

    /**
     * 建立優惠券
     */
    @PostMapping
    public ApiResponse<Map<String, Object>> createCoupon(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        String code = (String) request.get("code");
        String typeStr = (String) request.get("type");
        Object valueObj = request.get("value");

        if (name == null || code == null || typeStr == null || valueObj == null) {
            return ApiResponse.error("必填欄位不完整");
        }

        try {
            Coupon.CouponType type = Coupon.CouponType.valueOf(typeStr.toUpperCase());
            BigDecimal value = new BigDecimal(valueObj.toString());
            String description = (String) request.get("description");

            // 預設有效期 30 天
            LocalDateTime validFrom = LocalDateTime.now();
            LocalDateTime validUntil = validFrom.plusDays(30);

            Coupon coupon = couponService.createCoupon(name, code, type.name(), value, description, validFrom,
                    validUntil);
            return ApiResponse.ok(mapCoupon(coupon));
        } catch (Exception e) {
            return ApiResponse.error("建立失敗: " + e.getMessage());
        }
    }

    /**
     * 發放優惠券給單一會員
     */
    @PostMapping("/{couponId}/distribute/{memberId}")
    public ApiResponse<Void> distributeToMember(
            @PathVariable Long couponId,
            @PathVariable Long memberId) {
        try {
            couponService.distributeToMember(couponId, memberId);
            return ApiResponse.ok(null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 批量發放優惠券給會員標籤
     */
    @PostMapping("/{couponId}/distribute-by-tag/{tagId}")
    public ApiResponse<Map<String, Object>> distributeByTag(
            @PathVariable Long couponId,
            @PathVariable Long tagId) {
        try {
            List<Long> memberIds = tagService.getMembersByTag(tagId);
            int count = 0;
            for (Long memberId : memberIds) {
                try {
                    couponService.distributeToMember(couponId, memberId);
                    count++;
                } catch (Exception ignored) {
                    // 某些會員可能已有此優惠券
                }
            }
            Map<String, Object> result = new HashMap<>();
            result.put("totalMembers", memberIds.size());
            result.put("successCount", count);
            return ApiResponse.ok(result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 批量發放優惠券給等級
     */
    @PostMapping("/{couponId}/distribute-by-level/{levelId}")
    public ApiResponse<Void> distributeByLevel(
            @PathVariable Long couponId,
            @PathVariable Long levelId) {
        try {
            couponService.distributeToLevel(couponId, levelId);
            return ApiResponse.ok(null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 啟用/停用優惠券
     */
    @PutMapping("/{couponId}/toggle")
    public ApiResponse<Void> toggleCoupon(@PathVariable Long couponId) {
        return couponMapper.findById(couponId)
                .map(coupon -> {
                    coupon.setActive(!coupon.isActive());
                    couponMapper.update(coupon);
                    return ApiResponse.<Void>ok(null);
                })
                .orElse(ApiResponse.error("優惠券不存在"));
    }

    private Map<String, Object> mapCoupon(Coupon c) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", c.getId());
        map.put("name", c.getName());
        map.put("code", c.getCode());
        map.put("type", c.getDiscountType());
        map.put("value", c.getDiscountValue());
        map.put("description", c.getDescription());
        map.put("active", c.isActive());
        map.put("validFrom", c.getStartDate() != null ? c.getStartDate().toString() : null);
        map.put("validUntil", c.getEndDate() != null ? c.getEndDate().toString() : null);
        return map;
    }
}
