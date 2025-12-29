package com.toy.store.scheduler;

import com.toy.store.model.*;
import com.toy.store.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 機台自動上下架排程任務
 * 每分鐘檢查一次，自動根據設定的時間上架/下架遊戲
 */
@Slf4j
@Component
public class GameScheduleTask {

    @Autowired
    private IchibanBoxRepository ichibanBoxRepository;

    /**
     * 每分鐘執行一次上下架檢查
     */
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void checkGameSchedules() {
        LocalDateTime now = LocalDateTime.now();
        int launched = 0;
        int ended = 0;

        // 檢查一番賞
        List<IchibanBox> ichibanBoxes = ichibanBoxRepository.findAll();
        for (IchibanBox box : ichibanBoxes) {
            // 自動上架：DRAFT 狀態且已過開始時間
            if (box.getStatus() == IchibanBox.Status.DRAFT &&
                    box.getStartTime() != null &&
                    now.isAfter(box.getStartTime())) {
                box.setStatus(IchibanBox.Status.ACTIVE);
                ichibanBoxRepository.save(box);
                launched++;
                log.info("一番賞 [{}] 自動上架", box.getName());
            }
            // 自動下架：ACTIVE 狀態且已過結束時間
            if (box.getStatus() == IchibanBox.Status.ACTIVE &&
                    box.getEndTime() != null &&
                    now.isAfter(box.getEndTime())) {
                box.setStatus(IchibanBox.Status.ENDED);
                ichibanBoxRepository.save(box);
                ended++;
                log.info("一番賞 [{}] 自動下架", box.getName());
            }
        }

        if (launched > 0 || ended > 0) {
            log.info("定時上下架檢查完成：上架 {} 個，下架 {} 個", launched, ended);
        }
    }
}
