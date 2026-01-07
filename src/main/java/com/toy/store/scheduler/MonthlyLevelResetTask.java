package com.toy.store.scheduler;

import com.toy.store.model.Member;
import com.toy.store.model.MemberLevel;
import com.toy.store.mapper.MemberLevelMapper;
import com.toy.store.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MonthlyLevelResetTask {

    private final MemberMapper memberMapper;
    private final MemberLevelMapper memberLevelMapper;

    @Scheduled(cron = "0 0 0 1 * ?")
    @Transactional
    public void resetMemberLevels() {
        List<Member> members = memberMapper.findAll();
        List<MemberLevel> levels = memberLevelMapper.findByEnabledTrueOrderBySortOrderAsc();

        if (levels.isEmpty())
            return;

        for (Member member : members) {
            MemberLevel currentLevel = member.getLevel();
            if (currentLevel == null)
                continue;

            BigDecimal monthlyRecharge = member.getMonthlyRecharge();

            int currentIndex = -1;
            for (int i = 0; i < levels.size(); i++) {
                if (levels.get(i).getId().equals(currentLevel.getId())) {
                    currentIndex = i;
                    break;
                }
            }

            if (currentIndex > 0) {
                if (monthlyRecharge.compareTo(currentLevel.getThreshold()) < 0) {
                    member.setLevel(levels.get(currentIndex - 1));
                }
            }

            member.setMonthlyRecharge(BigDecimal.ZERO);
            memberMapper.update(member);
        }
    }
}
