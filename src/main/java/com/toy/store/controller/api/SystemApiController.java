package com.toy.store.controller.api;

import com.toy.store.dto.ApiResponse;
import com.toy.store.model.SystemSetting;
import com.toy.store.service.SystemSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 系統設定 API
 */
@RestController
@RequestMapping("/api/system")
public class SystemApiController {

    @Autowired
    private SystemSettingService settingService;

    /**
     * 取得公開設定（功能開關狀態）
     */
    @GetMapping("/settings")
    public ApiResponse<Map<String, Object>> getPublicSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("shoppingEnabled", settingService.isShoppingEnabled());
        settings.put("ichibanEnabled", settingService.isIchibanEnabled());
        settings.put("rouletteEnabled", settingService.isRouletteEnabled());
        settings.put("bingoEnabled", settingService.isBingoEnabled());
        settings.put("redeemEnabled", settingService.isRedeemEnabled());
        settings.put("luckyThreshold", settingService.getLuckyThreshold());
        return ApiResponse.ok(settings);
    }

    /**
     * 更新設定（後台用）
     */
    @PostMapping("/settings")
    public ApiResponse<Void> updateSetting(@RequestParam String key, @RequestParam String value) {
        try {
            settingService.updateSetting(key, value);
            return ApiResponse.ok(null, "設定已更新");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
