package com.toy.store.scheduler;

import com.toy.store.model.Member;
import com.toy.store.model.MemberLevel;
import com.toy.store.repository.MemberLevelRepository;
import com.toy.store.repository.MemberRepository;
import com.toy.store.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * VIP 保級排程任務
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VipReviewScheduler {

    private final MemberRepository memberRepository;
    private final MemberLevelRepository memberLevelRepository;
    private final MessageService messageService;

    @Scheduled(cron = "0 0 2 1 1,4,7,10 ?")
    @Transactional
    public void performQuarterlyVipReview() {
        log.info("開始執行季度 VIP 保級檢查...");

        List<Member> allMembers = memberRepository.findAll();
        List<MemberLevel> allLevels = memberLevelRepository.findByEnabledTrueOrderBySortOrderAsc();

        if (allLevels.isEmpty()) {
            log.warn("沒有啟用的會員等級，跳過保級檢查");
            return;
        }

        int reviewedCount = 0;
        int downgradedCount = 0;

        for (Member member : allMembers) {
            if (member.getLevel() == null)
                continue;

            MemberLevel currentLevel = member.getLevel();
            BigDecimal quarterSpend = getQuarterlySpend(member);

            BigDecimal retentionThreshold = currentLevel.getThreshold()
                    .multiply(BigDecimal.valueOf(0.5));

            if (quarterSpend.compareTo(retentionThreshold) < 0) {
                MemberLevel newLevel = findLowerLevel(currentLevel, allLevels);
                if (newLevel != null && !newLevel.equals(currentLevel)) {
                    member.setLevel(newLevel);
                    member.setLastLevelReviewDate(LocalDate.now());
                    memberRepository.save(member);

                    messageService.sendWarningMessage(
                            member.getId(),
                            "會員等級調整",
                            "由於本季消費未達保級門檻，您的等級已調整為【" + newLevel.getName() + "】。" +
                                    "請繼續消費以重新升級！");

                    downgradedCount++;
                    log.info("會員 {} 降級：{} -> {}", member.getUsername(),
                            currentLevel.getName(), newLevel.getName());
                }
            } else {
                member.setLastLevelReviewDate(LocalDate.now());
                memberRepository.save(member);
            }

            reviewedCount++;
        }

        log.info("季度 VIP 保級檢查完成：共檢查 {} 位會員，{} 位降級", reviewedCount, downgradedCount);
    }

    private BigDecimal getQuarterlySpend(Member member) {
        return member.getMonthlyRecharge() != null ? member.getMonthlyRecharge() : BigDecimal.ZERO;
    }

    private MemberLevel findLowerLevel(MemberLevel current, List<MemberLevel> allLevels) {
        MemberLevel lower = null;
        for (MemberLevel level : allLevels) {
            if (level.getSortOrder() < current.getSortOrder()) {
                if (lower == null || level.getSortOrder() > lower.getSortOrder()) {
                    lower = level;
                }
            }
        }
        return lower;
    }
}
