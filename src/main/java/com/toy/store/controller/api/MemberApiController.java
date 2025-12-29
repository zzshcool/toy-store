package com.toy.store.controller.api;

import com.toy.store.dto.ApiResponse;
import com.toy.store.model.Member;
import com.toy.store.repository.MemberRepository;
import com.toy.store.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * 會員 API - 餘額查詢等
 */
@RestController
@RequestMapping("/api/member")
public class MemberApiController {

    @Autowired
    private MemberRepository memberRepository;

    /**
     * 取得當前會員餘額
     */
    @GetMapping("/balance")
    public ApiResponse<Map<String, Object>> getBalance(HttpServletRequest request) {
        TokenService.TokenInfo user = (TokenService.TokenInfo) request.getAttribute("currentUser");
        if (user == null) {
            return ApiResponse.error("請先登入");
        }

        Member member = memberRepository.findByUsername(user.getUsername()).orElse(null);
        if (member == null) {
            return ApiResponse.error("會員不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("balance", member.getPlatformWalletBalance());
        result.put("levelName", member.getLevel() != null ? member.getLevel().getName() : "一般會員");
        result.put("shardBalance", member.getPoints());

        return ApiResponse.ok(result);
    }

    @Autowired
    private com.toy.store.service.MemberService memberService;

    @Autowired
    private com.toy.store.service.TokenService tokenService;

    /**
     * 登入 API（供沉浸式登入 Modal 使用）
     */
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(
            @RequestParam String username,
            @RequestParam String password,
            jakarta.servlet.http.HttpServletResponse response) {

        try {
            Member member = memberService.validateLogin(username, password);
            if (member == null) {
                return ApiResponse.error("帳號或密碼錯誤");
            }

            // 生成 Token 並設置 Cookie
            String token = tokenService.createToken(member.getUsername(), TokenService.ROLE_USER);
            jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("authToken", token);
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
}
