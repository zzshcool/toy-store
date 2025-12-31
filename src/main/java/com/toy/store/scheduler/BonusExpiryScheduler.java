package com.toy.store.scheduler;

import com.toy.store.model.Member;
import com.toy.store.repository.MemberRepository;
import com.toy.store.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 紅利點數過期排程任務
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BonusExpiryScheduler {

    private final MemberRepository memberRepository;
    private final MessageService messageService;

    private static final int BONUS_VALIDITY_DAYS = 365;
    private static final int REMINDER_DAYS_BEFORE = 7;

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

    @Scheduled(cron = "0 30 3 1 * ?")
    @Transactional
    public void clearExpiredBonus() {
        log.info("開始執行月度紅利清理...");
        log.info("月度紅利清理完成（模擬執行）");
    }
}
