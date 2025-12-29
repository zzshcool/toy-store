package com.toy.store.scheduler;

import com.toy.store.model.Member;
import com.toy.store.repository.MemberRepository;
import com.toy.store.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 紅利點數過期排程任務
 * 每日檢查即將過期的紅利並發送提醒
 */
@Slf4j
@Component
public class BonusExpiryScheduler {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MessageService messageService;

    // 紅利有效期（天）
    private static final int BONUS_VALIDITY_DAYS = 365;
    // 提前幾天提醒
    private static final int REMINDER_DAYS_BEFORE = 7;

    /**
     * 每日凌晨 3 點執行紅利過期檢查
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void checkBonusExpiry() {
        log.info("開始執行紅利過期檢查...");

        List<Member> membersWithBonus = memberRepository.findAll();
        int notifiedCount = 0;

        for (Member member : membersWithBonus) {
            if (member.getBonusPoints() == null || member.getBonusPoints() <= 0) {
                continue;
            }

            // 簡化處理：提醒有紅利的會員
            // 實際應有紅利獲取時間記錄，這裡假設紅利即將過期
            if (member.getBonusPoints() > 0) {
                messageService.sendWarningMessage(
                        member.getId(),
                        "紅利即將過期提醒",
                        "您有 " + member.getBonusPoints() + " 點紅利即將過期，" +
                                "請盡快使用以免浪費！");
                notifiedCount++;
            }
        }

        log.info("紅利過期檢查完成：發送 {} 條提醒", notifiedCount);
    }

    /**
     * 每月 1 號執行紅利清零（超過有效期的紅利）
     */
    @Scheduled(cron = "0 30 3 1 * ?")
    @Transactional
    public void clearExpiredBonus() {
        log.info("開始執行月度紅利清理...");

        // 這裡是簡化版本
        // 實際應該：
        // 1. 記錄每筆紅利的獲取時間
        // 2. 只清除超過 365 天的紅利
        // 目前僅做記錄，不實際清除

        log.info("月度紅利清理完成（模擬執行）");
    }
}
