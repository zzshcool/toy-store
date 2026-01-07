package com.toy.store.controller.api;

import com.toy.store.dto.ApiResponse;
import com.toy.store.model.GachaRecord;
import com.toy.store.mapper.GachaRecordMapper;
import com.toy.store.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 抽獎紀錄 API - 提供最新中獎資訊（跑馬燈）
 */
@RestController
@RequestMapping("/api/gacha")
@RequiredArgsConstructor
public class GachaRecordApiController {

    private final GachaRecordMapper gachaRecordMapper;
    private final MemberMapper memberMapper;

    @GetMapping("/recent")
    public ApiResponse<List<Map<String, Object>>> getRecentRecords(
            @RequestParam(defaultValue = "10") int limit) {

        List<GachaRecord> records = gachaRecordMapper.findTop20ByOrderByCreatedAtDesc();

        List<Map<String, Object>> result = records.stream()
                .limit(limit)
                .map(this::recordToPublicMap)
                .collect(Collectors.toList());

        return ApiResponse.ok(result);
    }

    private Map<String, Object> recordToPublicMap(GachaRecord record) {
        Map<String, Object> map = new HashMap<>();

        String nickname = memberMapper.findById(record.getMemberId())
                .map(m -> m.getNickname() != null ? m.getNickname() : m.getUsername())
                .orElse("玩家");
        if (nickname.length() > 2) {
            nickname = nickname.charAt(0) + "***" + nickname.charAt(nickname.length() - 1);
        }

        map.put("player", nickname);
        map.put("prizeName", record.getPrizeName());
        map.put("gachaType", record.getGachaType().name());
        map.put("prizeRank", record.getPrizeRank());
        map.put("isRare", record.getPrizeRank() != null &&
                (record.getPrizeRank().equals("A") || record.getPrizeRank().equals("LAST")));
        map.put("createdAt", record.getCreatedAt());

        return map;
    }
}
