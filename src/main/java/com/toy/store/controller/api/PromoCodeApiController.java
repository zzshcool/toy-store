package com.toy.store.controller.api;

import com.toy.store.annotation.CurrentUser;
import com.toy.store.dto.ApiResponse;
import com.toy.store.model.Member;
import com.toy.store.model.PromoCode;
import com.toy.store.mapper.MemberMapper;
import com.toy.store.service.PromoCodeService;
import com.toy.store.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 推薦碼/禮包碼 API
 */
@RestController
@RequestMapping("/api/promo")
@RequiredArgsConstructor
public class PromoCodeApiController {

    private final PromoCodeService promoCodeService;
    private final MemberMapper memberMapper;

    @GetMapping("/my-referral")
    public ApiResponse<Map<String, Object>> getMyReferralCode(
            @CurrentUser TokenService.TokenInfo tokenInfo) {

        if (tokenInfo == null) {
            return ApiResponse.error("請先登入");
        }

        return memberMapper.findByUsername(tokenInfo.getUsername())
                .map(member -> {
                    PromoCode code = promoCodeService.generateReferralCode(member.getId());
                    Map<String, Object> result = new HashMap<>();
                    result.put("code", code.getCode());
                    result.put("rewardValue", code.getRewardValue());
                    result.put("usedCount", code.getUsedCount());
                    return ApiResponse.ok(result);
                })
                .orElseGet(() -> ApiResponse.error("會員不存在"));
    }

    @PostMapping("/redeem")
    public ApiResponse<String> redeemCode(
            @CurrentUser TokenService.TokenInfo tokenInfo,
            @RequestBody Map<String, String> request) {

        if (tokenInfo == null) {
            return ApiResponse.error("請先登入");
        }

        return memberMapper.findByUsername(tokenInfo.getUsername())
                .map(member -> {
                    String code = request.get("code");
                    if (code == null || code.trim().isEmpty()) {
                        return ApiResponse.<String>error("請輸入兌換碼");
                    }

                    String result = promoCodeService.redeemCode(member.getId(), code.trim());

                    if (result.startsWith("兌換成功")) {
                        return ApiResponse.ok(result);
                    } else {
                        return ApiResponse.<String>error(result);
                    }
                })
                .orElseGet(() -> ApiResponse.error("會員不存在"));
    }
}
