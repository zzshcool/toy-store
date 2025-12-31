package com.toy.store.controller.api;

import com.toy.store.dto.ApiResponse;
import com.toy.store.model.Member;
import com.toy.store.repository.MemberRepository;
import com.toy.store.service.MemberService;
import com.toy.store.service.TokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 會員 API - 餘額查詢等
 */
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final TokenService tokenService;

    /**
     * 取得當前會員餘額
     */
    @GetMapping("/balance")
    public ApiResponse<Map<String, Object>> getBalance(HttpServletRequest request) {
        TokenService.TokenInfo user = (TokenService.TokenInfo) request.getAttribute("currentUser");
        if (user == null) {
            return ApiResponse.error("請先登入");
        }

        return memberRepository.findByUsername(user.getUsername())
                .map(member -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("balance", member.getPlatformWalletBalance());
                    result.put("levelName", member.getLevel() != null ? member.getLevel().getName() : "一般會員");
                    result.put("shardBalance", member.getPoints());
                    return ApiResponse.ok(result);
                })
                .orElseGet(() -> ApiResponse.error("會員不存在"));
    }

    /**
     * 登入 API（供沉浸式登入 Modal 使用）
     */
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletResponse response) {

        try {
            Member member = memberService.validateLogin(username, password);
            if (member == null) {
                return ApiResponse.error("帳號或密碼錯誤");
            }

            // 生成 Token 並設置 Cookie
            String token = tokenService.createToken(member.getUsername(), TokenService.ROLE_USER);
            Cookie cookie = new Cookie("authToken", token);
            cookie.setPath("/");
            cookie.setMaxAge(7 * 24 * 60 * 60); // 7 天
            cookie.setHttpOnly(true);
            response.addCookie(cookie);

            Map<String, Object> result = new HashMap<>();
            result.put("username", member.getUsername());
            result.put("nickname", member.getNickname());
            result.put("balance", member.getPlatformWalletBalance());

            return ApiResponse.ok(result, "登入成功！");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 獲取會員歷史紀錄（後台使用）
     */
    @GetMapping("/admin/{memberId}/history")
    public ApiResponse<Map<String, Object>> getMemberHistory(@PathVariable Long memberId) {
        return memberRepository.findById(memberId)
                .map(member -> {
                    Map<String, Object> history = new HashMap<>();
                    history.put("id", member.getId());
                    history.put("username", member.getUsername());
                    history.put("email", member.getEmail());
                    history.put("nickname", member.getNickname());
                    history.put("balance", member.getPlatformWalletBalance());
                    history.put("level", member.getLevel() != null ? member.getLevel().getName() : "一般會員");
                    history.put("points", member.getPoints());
                    history.put("bonusPoints", member.getBonusPoints());
                    history.put("growthValue", member.getGrowthValue());
                    history.put("monthlyRecharge", member.getMonthlyRecharge());
                    history.put("createdAt", member.getCreatedAt());
                    history.put("lastLoginTime", member.getLastLoginTime());
                    history.put("enabled", member.isEnabled());
                    return ApiResponse.ok(history);
                })
                .orElseGet(() -> ApiResponse.error("會員不存在"));
    }
}
