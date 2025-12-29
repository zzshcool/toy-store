package com.toy.store.controller.api;

import com.toy.store.dto.ApiResponse;
import com.toy.store.model.DrawVerification;
import com.toy.store.service.TransparencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 透明化驗證 API
 */
@RestController
@RequestMapping("/api/verification")
public class VerificationApiController {

    @Autowired
    private TransparencyService transparencyService;

    /**
     * 獲取最近完售的驗證記錄
     */
    @GetMapping("/completed")
    public ApiResponse<List<Map<String, Object>>> getCompletedVerifications() {
        List<DrawVerification> verifications = transparencyService.getCompletedVerifications();

        List<Map<String, Object>> result = new ArrayList<>();
        for (DrawVerification v : verifications) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", v.getId());
            item.put("gameType", v.getGameType());
            item.put("gameName", v.getGameName());
            item.put("hashValue", v.getHashValue());
            item.put("completedAt", v.getCompletedAt());
            result.add(item);
        }

        return ApiResponse.ok(result);
    }

    /**
     * 獲取特定遊戲的驗證詳情
     */
    @GetMapping("/{gameType}/{gameId}")
    public ApiResponse<Map<String, Object>> getVerificationDetail(
            @PathVariable String gameType,
            @PathVariable Long gameId) {

        DrawVerification.GameType type;
        try {
            type = DrawVerification.GameType.valueOf(gameType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("無效的遊戲類型");
        }

        return transparencyService.getVerification(type, gameId)
                .map(v -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("id", v.getId());
                    result.put("gameType", v.getGameType());
                    result.put("gameId", v.getGameId());
                    result.put("gameName", v.getGameName());
                    result.put("randomSeed", v.getRandomSeed());
                    result.put("hashValue", v.getHashValue());
                    result.put("resultJson", v.getResultJson());
                    result.put("completed", v.isCompleted());
                    result.put("completedAt", v.getCompletedAt());
                    result.put("createdAt", v.getCreatedAt());
                    return ApiResponse.ok(result);
                })
                .orElse(ApiResponse.error("找不到驗證記錄"));
    }

    /**
     * 驗證哈希值
     */
    @PostMapping("/verify")
    public ApiResponse<Map<String, Object>> verifyHash(@RequestBody Map<String, Object> request) {
        Long verificationId = Long.valueOf(request.get("verificationId").toString());
        String hash = (String) request.get("hash");

        boolean isValid = transparencyService.verifyHash(verificationId, hash);

        Map<String, Object> result = new HashMap<>();
        result.put("isValid", isValid);
        result.put("message", isValid ? "驗證通過！結果未被篡改。" : "驗證失敗！哈希值不匹配。");

        return ApiResponse.ok(result);
    }

    /**
     * 自行計算 SHA256（供用戶驗證）
     */
    @PostMapping("/compute-hash")
    public ApiResponse<Map<String, Object>> computeHash(@RequestBody Map<String, String> request) {
        String input = request.get("input");
        if (input == null || input.isEmpty()) {
            return ApiResponse.error("請提供輸入字符串");
        }

        String hash = transparencyService.computeSHA256(input);

        Map<String, Object> result = new HashMap<>();
        result.put("input", input);
        result.put("sha256", hash);

        return ApiResponse.ok(result);
    }
}
