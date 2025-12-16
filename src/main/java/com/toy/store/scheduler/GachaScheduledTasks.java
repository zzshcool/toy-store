package com.toy.store.scheduler;

import com.toy.store.service.IchibanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 抽獎系統排程任務
 */
@Component
public class GachaScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(GachaScheduledTasks.class);

    @Autowired
    private IchibanService ichibanService;

    /**
     * 每分鐘釋放過期的一番賞格子鎖定
     * 鎖定時間為3分鐘，超過後自動釋放讓其他玩家選擇
     */
    @Scheduled(fixedRate = 60000) // 每60秒執行一次
    public void releaseExpiredSlotLocks() {
        try {
            int released = ichibanService.releaseExpiredLocks();
            if (released > 0) {
                logger.info("已釋放 {} 個過期的一番賞格子鎖定", released);
            }
        } catch (Exception e) {
            logger.error("釋放過期鎖定時發生錯誤: {}", e.getMessage());
        }
    }
}
