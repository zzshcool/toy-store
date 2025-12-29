package com.toy.store.service;

import com.toy.store.model.MemberMission;
import com.toy.store.repository.MemberMissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final MemberMissionRepository missionRepository;
    private final MemberService memberService;

    /**
     * 更新任務進度
     */
    @Transactional
    public void updateMissionProgress(Long memberId, MemberMission.MissionType type, int amount) {
        LocalDate today = LocalDate.now();
        Optional<MemberMission> missionOpt = missionRepository.findByMemberIdAndMissionDateAndType(memberId, today,
                type);

        if (missionOpt.isPresent()) {
            MemberMission mission = missionOpt.get();
            if (!mission.isCompleted()) {
                mission.addProgress(amount);
                if (mission.isCompleted()) {
                    // 自動領取獎勵
                    memberService.addBonusPoints(memberId, mission.getRewardBonusPoints());
                    mission.setRewardClaimed(true);
                }
                missionRepository.save(mission);
            }
        } else {
            // 如果今日任務尚未初始化，先初始化
            initDailyMissions(memberId);
            // 遞迴調用以執行更新
            missionRepository.findByMemberIdAndMissionDateAndType(memberId, today, type)
                    .ifPresent(m -> {
                        if (!m.isCompleted()) {
                            m.addProgress(amount);
                            if (m.isCompleted()) {
                                memberService.addBonusPoints(memberId, m.getRewardBonusPoints());
                                m.setRewardClaimed(true);
                            }
                            missionRepository.save(m);
                        }
                    });
        }
    }

    /**
     * 初始化今日任務
     */
    @Transactional
    public void initDailyMissions(Long memberId) {
        LocalDate today = LocalDate.now();
        List<MemberMission> existing = missionRepository.findByMemberIdAndMissionDate(memberId, today);
        if (existing.isEmpty()) {
            // 從規格書 7.C 定義基礎任務
            // 1. 每日登入
            createMission(memberId, today, MemberMission.MissionType.DAILY_LOGIN, 1, 10);
            // 2. 消費任務 (例如：累計消費 500)
            createMission(memberId, today, MemberMission.MissionType.SPEND_AMOUNT, 500, 20);
            // 3. 抽獎次數 (例如：累計抽獎 10 次)
            createMission(memberId, today, MemberMission.MissionType.DRAW_COUNT, 10, 30);
        }
    }

    private void createMission(Long memberId, LocalDate date, MemberMission.MissionType type, int target, int reward) {
        MemberMission mission = new MemberMission();
        mission.setMemberId(memberId);
        mission.setMissionDate(date);
        mission.setType(type);
        mission.setTargetValue(target);
        mission.setRewardBonusPoints(reward);
        missionRepository.save(mission);
    }
}
