package com.toy.store.service;

import com.toy.store.model.MemberMission;
import com.toy.store.model.MemberSignIn;
import com.toy.store.mapper.MemberSignInMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SignInService {

    private final MemberSignInMapper signInMapper;
    private final MemberService memberService;
    private final MissionService missionService;
    private final SystemSettingService settingService;

    public SignInService(
            MemberSignInMapper signInMapper,
            @Lazy MemberService memberService,
            @Lazy MissionService missionService,
            @Lazy SystemSettingService settingService) {
        this.signInMapper = signInMapper;
        this.memberService = memberService;
        this.missionService = missionService;
        this.settingService = settingService;
    }

    @Transactional
    public void processDailySignIn(Long memberId) {
        LocalDate today = LocalDate.now();
        Optional<MemberSignIn> existing = signInMapper.findByMemberIdAndSignInDate(memberId, today);

        if (existing.isPresent()) {
            return; // Already signed in today
        }

        LocalDate yesterday = today.minusDays(1);
        Optional<MemberSignIn> lastSignIn = signInMapper.findByMemberIdAndSignInDate(memberId, yesterday);

        int consecutiveDays = 1;
        if (lastSignIn.isPresent()) {
            consecutiveDays = (lastSignIn.get().getConsecutiveDays() % 7) + 1;
        }

        MemberSignIn signIn = new MemberSignIn();
        signIn.setMemberId(memberId);
        signIn.setSignInDate(today);
        signIn.setConsecutiveDays(consecutiveDays);
        signIn.setRewardPoints(0);
        signIn.setCreatedAt(LocalDateTime.now());
        signInMapper.insert(signIn);

        // 從系統設定取得簽到獎勵值
        int dailyReward = settingService.getSignInDailyReward();
        int weeklyBonus = settingService.getSignInWeeklyBonus();

        // 1~6 日: 每日獎勵, 7 日: 每日獎勵 + 週獎勵
        int reward = (consecutiveDays == 7) ? (dailyReward + weeklyBonus) : dailyReward;
        memberService.addBonusPoints(memberId, reward);

        // Also trigger daily login mission
        missionService.updateMissionProgress(memberId, MemberMission.MissionType.DAILY_LOGIN, 1);
    }
}
