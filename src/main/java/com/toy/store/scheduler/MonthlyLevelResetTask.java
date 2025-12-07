package com.toy.store.scheduler;

import com.toy.store.model.Member;
import com.toy.store.model.MemberLevel;
import com.toy.store.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
public class MonthlyLevelResetTask {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private com.toy.store.repository.MemberLevelRepository memberLevelRepository;

    // Run at 00:00 on the 1st day of every month
    @Scheduled(cron = "0 0 0 1 * ?")
    @Transactional
    public void resetMemberLevels() {
        List<Member> members = memberRepository.findAll();
        List<MemberLevel> levels = memberLevelRepository.findByEnabledTrueOrderBySortOrderAsc();

        if (levels.isEmpty())
            return;

        for (Member member : members) {
            MemberLevel currentLevel = member.getLevel();
            if (currentLevel == null)
                continue;

            BigDecimal monthlyRecharge = member.getMonthlyRecharge();

            // Find current level index
            int currentIndex = -1;
            for (int i = 0; i < levels.size(); i++) {
                if (levels.get(i).getId().equals(currentLevel.getId())) {
                    currentIndex = i;
                    break;
                }
            }

            // Downgrade if threshold not met and not the lowest level
            if (currentIndex > 0) {
                if (monthlyRecharge.compareTo(currentLevel.getThreshold()) < 0) {
                    // Downgrade to previous level
                    member.setLevel(levels.get(currentIndex - 1));
                }
            }

            // Reset monthly recharge accumulation
            member.setMonthlyRecharge(BigDecimal.ZERO);
            memberRepository.save(member);
        }
    }
}
