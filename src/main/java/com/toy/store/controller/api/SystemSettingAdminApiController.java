package com.toy.store.controller.api;

import com.toy.store.dto.ApiResponse;
import com.toy.store.model.SystemSetting;
import com.toy.store.service.SystemSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 系統設定後台管理 API
 */
@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasAuthority('SYSTEM_SETTING')")
public class SystemSettingAdminApiController {

    private final SystemSettingService settingService;

    /**
     * 取得所有設定
     */
    @GetMapping
    public ApiResponse<List<Map<String, Object>>> getAllSettings() {
        List<SystemSetting> settings = settingService.getAllSettings();
        List<Map<String, Object>> result = settings.stream()
                .map(this::mapSetting)
                .collect(Collectors.toList());
        return ApiResponse.ok(result);
    }

    /**
     * 更新單一設定
     */
    @PutMapping("/{key}")
    public ApiResponse<Void> updateSetting(@PathVariable String key, @RequestBody Map<String, String> request) {
        String value = request.get("value");
        if (value == null) {
            return ApiResponse.error("缺少 value 欄位");
        }
        settingService.updateSetting(key, value);
        return ApiResponse.ok(null);
    }

    /**
     * 批量更新設定
     */
    @PutMapping
    public ApiResponse<Void> batchUpdateSettings(@RequestBody Map<String, String> settings) {
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            settingService.updateSetting(entry.getKey(), entry.getValue());
        }
        return ApiResponse.ok(null);
    }

    private Map<String, Object> mapSetting(SystemSetting setting) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", setting.getId());
        map.put("key", setting.getSettingKey());
        map.put("value", setting.getSettingValue());
        map.put("description", setting.getDescription());
        return map;
    }
}
