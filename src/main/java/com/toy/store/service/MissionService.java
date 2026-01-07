package com.toy.store.service;

import com.toy.store.model.MemberMission;
import com.toy.store.mapper.MemberMissionMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MissionService {

    private final MemberMissionMapper missionMapper;
    private final MemberService memberService;
    private final SystemSettingService settingService;

    public MissionService(
            MemberMissionMapper missionMapper,
            @Lazy MemberService memberService,
            @Lazy SystemSettingService settingService) {
        this.missionMapper = missionMapper;
        this.memberService = memberService;
        this.settingService = settingService;
    }

    /**
     * 更新任務進度
     */
    @Transactional
    public void updateMissionProgress(Long memberId, MemberMission.MissionType type, int amount) {
        LocalDate today = LocalDate.now();
        Optional<MemberMission> missionOpt = missionMapper.findByMemberIdAndMissionDateAndType(
                memberId, today, type.name());

        if (missionOpt.isPresent()) {
            MemberMission mission = missionOpt.get();
            if (!mission.isCompleted()) {
                mission.addProgress(amount);
                if (mission.isCompleted()) {
                    // 自動領取獎勵
                    memberService.addBonusPoints(memberId, mission.getRewardBonusPoints());
                    mission.setRewardClaimed(true);
                }
                missionMapper.update(mission);
            }
        } else {
            // 如果今日任務尚未初始化，先初始化
            initDailyMissions(memberId);
            // 遞迴調用以執行更新
            missionMapper.findByMemberIdAndMissionDateAndType(memberId, today, type.name())
                    .ifPresent(m -> {
                        if (!m.isCompleted()) {
                            m.addProgress(amount);
                            if (m.isCompleted()) {
                                memberService.addBonusPoints(memberId, m.getRewardBonusPoints());
                                m.setRewardClaimed(true);
                            }
                            missionMapper.update(m);
                        }
                    });
        }
    }

    /**
     * 初始化今日任務（從系統設定取得獎勵和目標值）
     */
    @Transactional
    public void initDailyMissions(Long memberId) {
        LocalDate today = LocalDate.now();
        List<MemberMission> existing = missionMapper.findByMemberIdAndMissionDate(memberId, today);
        if (existing.isEmpty()) {
            // 從系統設定取得任務配置
            int loginReward = settingService.getMissionDailyLoginReward();
            int spendTarget = settingService.getMissionSpendTarget();
            int spendReward = settingService.getMissionSpendReward();
            int drawTarget = settingService.getMissionDrawTarget();
            int drawReward = settingService.getMissionDrawReward();

            // 1. 每日登入
            createMission(memberId, today, MemberMission.MissionType.DAILY_LOGIN, 1, loginReward);
            // 2. 消費任務
            createMission(memberId, today, MemberMission.MissionType.SPEND_AMOUNT, spendTarget, spendReward);
            // 3. 抽獎次數
            createMission(memberId, today, MemberMission.MissionType.DRAW_COUNT, drawTarget, drawReward);
        }
    }

    private void createMission(Long memberId, LocalDate date, MemberMission.MissionType type, int target, int reward) {
        MemberMission mission = new MemberMission();
        mission.setMemberId(memberId);
        mission.setMissionDate(date);
        mission.setType(type);
        mission.setTargetValue(target);
        mission.setRewardBonusPoints(reward);
        mission.setCurrentProgress(0);
        mission.setIsCompleted(false);
        mission.setRewardClaimed(false);
        mission.setCreatedAt(LocalDateTime.now());
        missionMapper.insert(mission);
    }
}
