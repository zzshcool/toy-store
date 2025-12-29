package com.toy.store.controller.api;

import com.toy.store.dto.ApiResponse;
import com.toy.store.service.LogisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 物流管理 API（後台使用）
 */
@RestController
@RequestMapping("/api/admin/logistics")
public class LogisticsApiController {

    @Autowired
    private LogisticsService logisticsService;

    /**
     * 批次匯入物流單號
     * 
     * 請求格式：
     * [
     * {"shipmentId": "1", "trackingNo": "12345678", "provider": "TCAT"},
     * {"shipmentId": "2", "trackingNo": "87654321", "provider": "SEVEN"}
     * ]
     */
    @PostMapping("/batch-import")
    public ApiResponse<Map<String, Object>> batchImport(@RequestBody List<Map<String, String>> imports) {
        if (imports == null || imports.isEmpty()) {
            return ApiResponse.error("請提供匯入資料");
        }

        Map<String, Object> result = logisticsService.batchImportTrackingNumbers(imports);
        return ApiResponse.ok(result);
    }

    /**
     * 解析 CSV 格式匯入（簡化版）
     * 格式：發貨單ID,物流單號,物流商
     */
    @PostMapping("/import-csv")
    public ApiResponse<Map<String, Object>> importFromCsv(@RequestBody String csvContent) {
        if (csvContent == null || csvContent.trim().isEmpty()) {
            return ApiResponse.error("請提供 CSV 資料");
        }

        List<Map<String, String>> imports = new java.util.ArrayList<>();
        String[] lines = csvContent.split("\n");

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#"))
                continue;

            String[] parts = trimmed.split(",");
            if (parts.length >= 2) {
                Map<String, String> item = new java.util.HashMap<>();
                item.put("shipmentId", parts[0].trim());
                item.put("trackingNo", parts[1].trim());
                item.put("provider", parts.length > 2 ? parts[2].trim() : "TCAT");
                imports.add(item);
            }
        }

        Map<String, Object> result = logisticsService.batchImportTrackingNumbers(imports);
        return ApiResponse.ok(result);
    }
}
