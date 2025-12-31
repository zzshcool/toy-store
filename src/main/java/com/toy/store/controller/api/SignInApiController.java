package com.toy.store.controller.api;

import com.toy.store.annotation.CurrentUser;
import com.toy.store.dto.ApiResponse;
import com.toy.store.model.MemberSignIn;
import com.toy.store.repository.MemberRepository;
import com.toy.store.repository.MemberSignInRepository;
import com.toy.store.service.SignInService;
import com.toy.store.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 簽到系統 API
 */
@RestController
@RequestMapping("/api/member/sign-in")
@RequiredArgsConstructor
public class SignInApiController {

    private final SignInService signInService;
    private final MemberSignInRepository signInRepository;
    private final MemberRepository memberRepository;

    /**
     * 執行每日簽到
     */
    @PostMapping
    public ApiResponse<Map<String, Object>> doSignIn(@CurrentUser TokenService.TokenInfo tokenInfo) {
        if (tokenInfo == null) {
            return ApiResponse.error("請先登入");
        }

        return memberRepository.findByUsername(tokenInfo.getUsername())
                .map(member -> {
                    // 檢查今日是否已簽到
                    Optional<MemberSignIn> todaySignIn = signInRepository.findByMemberIdAndSignInDate(
                            member.getId(), LocalDate.now());

                    if (todaySignIn.isPresent()) {
                        Map<String, Object> result = new HashMap<>();
                        result.put("alreadySigned", true);
                        result.put("consecutiveDays", todaySignIn.get().getConsecutiveDays());
                        result.put("message", "今日已簽到");
                        return ApiResponse.ok(result);
                    }

                    // 執行簽到
                    signInService.processDailySignIn(member.getId());

                    // 重新查詢獲取結果
                    MemberSignIn newSignIn = signInRepository.findByMemberIdAndSignInDate(
                            member.getId(), LocalDate.now()).orElse(null);

                    Map<String, Object> result = new HashMap<>();
                    result.put("success", true);
                    result.put("consecutiveDays", newSignIn != null ? newSignIn.getConsecutiveDays() : 1);

                    // 計算獎勵
                    int reward = (newSignIn != null && newSignIn.getConsecutiveDays() == 7) ? 50 : 10;
                    result.put("reward", reward);
                    result.put("message", "簽到成功！獲得 " + reward + " 紅利點數");

                    return ApiResponse.ok(result);
                })
                .orElseGet(() -> ApiResponse.error("會員不存在"));
    }

    /**
     * 獲取今日簽到狀態
     */
    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> getSignInStatus(@CurrentUser TokenService.TokenInfo tokenInfo) {
        if (tokenInfo == null) {
            return ApiResponse.error("請先登入");
        }

        return memberRepository.findByUsername(tokenInfo.getUsername())
                .map(member -> {
                    LocalDate today = LocalDate.now();
                    Optional<MemberSignIn> todaySignIn = signInRepository.findByMemberIdAndSignInDate(
                            member.getId(), today);

                    Map<String, Object> status = new HashMap<>();
                    status.put("signedToday", todaySignIn.isPresent());
                    status.put("consecutiveDays", todaySignIn.map(MemberSignIn::getConsecutiveDays).orElse(0));

                    // 計算今日可獲得獎勵
                    int consecutiveDays = todaySignIn.map(MemberSignIn::getConsecutiveDays).orElse(0);
                    int nextDay = todaySignIn.isPresent() ? consecutiveDays : (consecutiveDays % 7) + 1;
                    int nextReward = (nextDay == 7) ? 50 : 10;
                    status.put("nextReward", nextReward);

                    return ApiResponse.ok(status);
                })
                .orElseGet(() -> ApiResponse.error("會員不存在"));
    }
}
