package com.toy.store.service;

import com.toy.store.model.MemberMission;
import com.toy.store.model.MemberSignIn;
import com.toy.store.repository.MemberSignInRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SignInService {

    private final MemberSignInRepository signInRepository;
    @Lazy
    private final MemberService memberService;
    @Lazy
    private final MissionService missionService;

    @Transactional
    public void processDailySignIn(Long memberId) {
        LocalDate today = LocalDate.now();
        Optional<MemberSignIn> existing = signInRepository.findByMemberIdAndSignInDate(memberId, today);

        if (existing.isPresent()) {
            return; // Already signed in today
        }

        LocalDate yesterday = today.minusDays(1);
        Optional<MemberSignIn> lastSignIn = signInRepository.findByMemberIdAndSignInDate(memberId, yesterday);

        int consecutiveDays = 1;
        if (lastSignIn.isPresent()) {
            consecutiveDays = (lastSignIn.get().getConsecutiveDays() % 7) + 1;
        }

        MemberSignIn signIn = new MemberSignIn(memberId, today, consecutiveDays);
        signInRepository.save(signIn);

        // Award bonus points based on spec 7.C
        // 1~6 日: 10 點, 7 日: 50 點
        int reward = (consecutiveDays == 7) ? 50 : 10;
        memberService.addBonusPoints(memberId, reward);

        // Also trigger daily login mission
        missionService.updateMissionProgress(memberId, MemberMission.MissionType.DAILY_LOGIN, 1);
    }
}
