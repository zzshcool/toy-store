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
        result.put("shardBalance", 0); // TODO: 碎片餘額

        return ApiResponse.ok(result);
    }
}
