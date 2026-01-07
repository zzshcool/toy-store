package com.toy.store.service;

import com.toy.store.model.*;
import com.toy.store.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 物流服務（含批次匯入）
 */
@Slf4j
@Service
public class LogisticsService {

    private final LogisticsRecordMapper logisticsMapper;
    private final ShipmentRequestMapper shipmentMapper;

    public LogisticsService(
            LogisticsRecordMapper logisticsMapper,
            ShipmentRequestMapper shipmentMapper) {
        this.logisticsMapper = logisticsMapper;
        this.shipmentMapper = shipmentMapper;
    }

    /**
     * 批次匯入物流單號
     * 
     * @param imports 格式：[{shipmentId, trackingNo, provider}]
     * @return 匯入結果
     */
    @Transactional
    public Map<String, Object> batchImportTrackingNumbers(List<Map<String, String>> imports) {
        int success = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        for (Map<String, String> item : imports) {
            try {
                Long shipmentId = Long.parseLong(item.get("shipmentId"));
                String trackingNo = item.get("trackingNo");
                String providerStr = item.get("provider");

                // 驗證發貨申請存在
                ShipmentRequest shipment = shipmentMapper.findById(shipmentId).orElse(null);
                if (shipment == null) {
                    errors.add("發貨單 " + shipmentId + " 不存在");
                    failed++;
                    continue;
                }

                // 解析物流商
                LogisticsRecord.LogisticsProvider provider;
                try {
                    provider = LogisticsRecord.LogisticsProvider.valueOf(providerStr.toUpperCase());
                } catch (Exception e) {
                    provider = LogisticsRecord.LogisticsProvider.TCAT; // 默認黑貓
                }

                // 創建或更新物流記錄
                LogisticsRecord record = logisticsMapper.findByShipmentId(shipmentId)
                        .orElseGet(LogisticsRecord::new);
                record.setShipmentId(shipmentId);
                record.setTrackingNo(trackingNo);
                record.setProvider(provider);
                record.setStatus(LogisticsRecord.LogisticsStatus.SHIPPED);
                record.setLastUpdate(LocalDateTime.now());

                if (record.getId() == null) {
                    logisticsMapper.insert(record);
                } else {
                    logisticsMapper.update(record);
                }

                // 更新發貨申請狀態
                shipment.setTrackingNumber(trackingNo);
                shipment.setStatusEnum(ShipmentRequest.Status.SHIPPED);
                shipmentMapper.update(shipment);

                success++;
            } catch (Exception e) {
                errors.add("處理錯誤: " + e.getMessage());
                failed++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("failed", failed);
        result.put("errors", errors);

        log.info("批次匯入物流單號完成：成功 {}，失敗 {}", success, failed);
        return result;
    }

    /**
     * 更新物流狀態
     */
    @Transactional
    public void updateLogisticsStatus(String trackingNo, LogisticsRecord.LogisticsStatus status) {
        logisticsMapper.findByTrackingNo(trackingNo).ifPresent(record -> {
            record.setStatus(status);
            record.setLastUpdate(LocalDateTime.now());
            if (status == LogisticsRecord.LogisticsStatus.DELIVERED) {
                record.setDeliveredAt(LocalDateTime.now());
            }
            logisticsMapper.update(record);
        });
    }

    /**
     * 查詢物流狀態
     */
    public Optional<LogisticsRecord> getLogisticsRecord(Long shipmentId) {
        return logisticsMapper.findByShipmentId(shipmentId);
    }

    /**
     * 獲取待處理的物流記錄
     */
    public List<LogisticsRecord> getPendingLogistics() {
        return logisticsMapper.findByStatus(LogisticsRecord.LogisticsStatus.PENDING.name());
    }
}
