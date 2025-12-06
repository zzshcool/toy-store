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

    // Run at 00:00 on the 1st day of every month
    @Scheduled(cron = "0 0 0 1 * ?")
    @Transactional
    public void resetMemberLevels() {
        List<Member> members = memberRepository.findAll();
        for (Member member : members) {
            MemberLevel currentLevel = member.getLevel();
            BigDecimal monthlyRecharge = member.getMonthlyRecharge();

            // Logic: "If unable to reach standard, return to previous level"
            // We check if their monthly recharge met the CURRENT level's threshold.
            // If not, downgrade.
            // Exception: Common level cannot downgrade.

            if (currentLevel != MemberLevel.COMMON) {
                if (monthlyRecharge.compareTo(currentLevel.getThreshold()) < 0) {
                    member.setLevel(currentLevel.previous());
                }
            }

            // Reset monthly recharge accumulation
            member.setMonthlyRecharge(BigDecimal.ZERO);
            memberRepository.save(member);
        }
    }
}
