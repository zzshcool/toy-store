package com.toy.store.scheduler;

import com.toy.store.service.IchibanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 抽獎系統排程任務
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GachaScheduledTasks {

    private final IchibanService ichibanService;

    @Scheduled(fixedRate = 60000)
    public void releaseExpiredSlotLocks() {
        try {
            int released = ichibanService.releaseExpiredLocks();
            if (released > 0) {
                log.info("已釋放 {} 個過期的一番賞格子鎖定", released);
            }
        } catch (Exception e) {
            log.error("釋放過期鎖定時發生錯誤: {}", e.getMessage());
        }
    }
}
