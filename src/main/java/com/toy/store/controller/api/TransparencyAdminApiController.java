package com.toy.store.controller.api;

import com.toy.store.dto.ApiResponse;
import com.toy.store.model.DrawVerification;
import com.toy.store.service.TransparencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 開獎透明查詢後台 API
 */
@RestController
@RequestMapping("/api/admin/transparency")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasAuthority('GACHA_MANAGE')")
public class TransparencyAdminApiController {

    private final TransparencyService transparencyService;

    /**
     * 取得已完售遊戲的驗證記錄
     */
    @GetMapping("/completed")
    public ApiResponse<List<Map<String, Object>>> getCompletedVerifications() {
        List<DrawVerification> verifications = transparencyService.getCompletedVerifications();
        List<Map<String, Object>> result = verifications.stream()
                .map(this::mapVerification)
                .collect(Collectors.toList());
        return ApiResponse.ok(result);
    }

    /**
     * 取得特定遊戲的驗證記錄
     */
    @GetMapping("/{gameType}/{gameId}")
    public ApiResponse<Map<String, Object>> getVerification(
            @PathVariable String gameType,
            @PathVariable Long gameId) {
        try {
            DrawVerification.GameType type = DrawVerification.GameType.valueOf(gameType.toUpperCase());
            return transparencyService.getVerification(type, gameId)
                    .map(v -> ApiResponse.ok(mapVerification(v)))
                    .orElse(ApiResponse.error("找不到該遊戲的驗證記錄"));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("無效的遊戲類型");
        }
    }

    /**
     * 驗證哈希值
     */
    @PostMapping("/verify/{id}")
    public ApiResponse<Map<String, Object>> verifyHash(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String providedHash = request.get("hash");
        if (providedHash == null || providedHash.trim().isEmpty()) {
            return ApiResponse.error("請提供哈希值");
        }

        boolean isValid = transparencyService.verifyHash(id, providedHash);
        Map<String, Object> result = new HashMap<>();
        result.put("valid", isValid);
        result.put("message", isValid ? "驗證成功" : "驗證失敗：哈希值不匹配");
        return ApiResponse.ok(result);
    }

    private Map<String, Object> mapVerification(DrawVerification v) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", v.getId());
        map.put("gameType", v.getGameType().name());
        map.put("gameId", v.getGameId());
        map.put("gameName", v.getGameName());
        map.put("hashValue", v.getHashValue());
        map.put("completed", v.isCompleted());
        map.put("createdAt", v.getCreatedAt() != null ? v.getCreatedAt().toString() : null);
        map.put("completedAt", v.getCompletedAt() != null ? v.getCompletedAt().toString() : null);
        // 只在需要時返回結果 JSON
        if (v.isCompleted() && v.getResultJson() != null) {
            map.put("resultJson", v.getResultJson());
        }
        return map;
    }
}
