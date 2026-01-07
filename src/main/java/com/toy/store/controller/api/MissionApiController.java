package com.toy.store.controller.api;

import com.toy.store.annotation.CurrentUser;
import com.toy.store.dto.ApiResponse;
import com.toy.store.model.MemberMission;
import com.toy.store.mapper.MemberMissionMapper;
import com.toy.store.mapper.MemberMapper;
import com.toy.store.service.MissionService;
import com.toy.store.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任務系統 API
 */
@RestController
@RequestMapping("/api/member/missions")
@RequiredArgsConstructor
public class MissionApiController {

    private final MissionService missionService;
    private final MemberMissionMapper missionMapper;
    private final MemberMapper memberMapper;

    /**
     * 獲取今日任務列表
     */
    @GetMapping
    public ApiResponse<List<Map<String, Object>>> getTodayMissions(@CurrentUser TokenService.TokenInfo tokenInfo) {
        if (tokenInfo == null) {
            return ApiResponse.error("請先登入");
        }

        return memberMapper.findByUsername(tokenInfo.getUsername())
                .map(member -> {
                    // 初始化今日任務（如果尚未初始化）
                    missionService.initDailyMissions(member.getId());

                    // 獲取今日任務
                    LocalDate today = LocalDate.now();
                    List<MemberMission> missions = missionMapper.findByMemberIdAndMissionDate(
                            member.getId(), today);

                    List<Map<String, Object>> result = new ArrayList<>();
                    for (MemberMission mission : missions) {
                        Map<String, Object> m = new HashMap<>();
                        m.put("id", mission.getId());
                        m.put("type", mission.getType().name());
                        m.put("typeName", getMissionTypeName(mission.getType()));
                        m.put("currentValue", mission.getCurrentProgress());
                        m.put("targetValue", mission.getTargetValue());
                        m.put("progress", calculateProgress(mission.getCurrentProgress(), mission.getTargetValue()));
                        m.put("completed", mission.isCompleted());
                        m.put("rewardClaimed", mission.isRewardClaimed());
                        m.put("rewardBonusPoints", mission.getRewardBonusPoints());
                        result.add(m);
                    }

                    return ApiResponse.ok(result);
                })
                .orElseGet(() -> ApiResponse.error("會員不存在"));
    }

    private String getMissionTypeName(MemberMission.MissionType type) {
        switch (type) {
            case DAILY_LOGIN:
                return "每日登入";
            case SPEND_AMOUNT:
                return "累計消費";
            case DRAW_COUNT:
                return "抽獎次數";
            default:
                return type.name();
        }
    }

    private int calculateProgress(int current, int target) {
        if (target == 0)
            return 100;
        return Math.min(100, (current * 100) / target);
    }
}
