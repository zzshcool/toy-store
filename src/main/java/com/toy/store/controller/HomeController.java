package com.toy.store.controller;

import com.toy.store.model.*;
import com.toy.store.mapper.*;
import com.toy.store.service.MissionService;
import com.toy.store.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class HomeController {

        private final ActivityMapper activityMapper;
        private final MemberActionLogMapper memberActionLogMapper;
        private final MemberMapper memberMapper;
        private final CarouselSlideMapper carouselSlideMapper;
        private final FeaturedItemMapper featuredItemMapper;
        private final GachaRecordMapper gachaRecordMapper;
        private final IchibanBoxMapper ichibanBoxMapper;
        private final BingoGameMapper bingoGameMapper;
        private final MissionService missionService;
        private final MemberMissionMapper memberMissionMapper;
        private final MemberSignInMapper memberSignInMapper;

        @GetMapping("/")
        public String home(Model model, HttpServletRequest request) {
                // Log Action (if user is logged in)
                TokenService.TokenInfo info = (TokenService.TokenInfo) request.getAttribute("currentUser");

                if (info != null && TokenService.ROLE_USER.equals(info.getRole())) {
                        memberMapper.findByUsername(info.getUsername()).ifPresent(member -> {
                                model.addAttribute("currentMember", member);

                                // 初始化今日任務並獲取進度
                                missionService.initDailyMissions(member.getId());
                                model.addAttribute("dailyMissions",
                                                memberMissionMapper.findByMemberIdAndMissionDate(member.getId(),
                                                                LocalDate.now()));

                                // 獲取今日簽到狀況
                                model.addAttribute("signInInfo",
                                                memberSignInMapper
                                                                .findByMemberIdAndSignInDate(member.getId(),
                                                                                LocalDate.now())
                                                                .orElse(null));

                                // 記錄操作日誌
                                MemberActionLog log = new MemberActionLog();
                                log.setMemberId(member.getId());
                                log.setMemberUsername(member.getUsername());
                                log.setAction("VIEW_HOME");
                                log.setDetails("Viewed Active Activities");
                                log.setSuccess(true);
                                log.setTimestamp(LocalDateTime.now());
                                memberActionLogMapper.insert(log);
                        });
                }

                // Fetch active activities from DB
                List<Activity> activities = activityMapper.findAll().stream()
                                .filter(Activity::isActive)
                                .collect(Collectors.toList());

                model.addAttribute("activities", activities);

                // Platform Data
                model.addAttribute("carouselSlides", carouselSlideMapper.findAllByOrderBySortOrderAsc());
                // model.addAttribute("newArrivals", featuredItemMapper
                // .findByItemTypeOrderBySortOrderAsc(FeaturedItem.Type.NEW_ARRIVAL.name()));
                // model.addAttribute("bestSellers", featuredItemMapper
                // .findByItemTypeOrderBySortOrderAsc(FeaturedItem.Type.BEST_SELLER.name()));

                // Commercial Optimization Data
                List<GachaRecord> records = gachaRecordMapper.findTop20ByOrderByCreatedAtDesc();
                List<Map<String, Object>> latestWinners = records.stream().map(record -> {
                        Map<String, Object> map = new HashMap<>();
                        Long memberId = record.getMemberId();
                        String nickname = (memberId != null) ? memberMapper.findById(memberId)
                                        .map(m -> m.getNickname() != null ? m.getNickname() : m.getUsername())
                                        .orElse("玩家") : "玩家";

                        // Anonymize
                        if (nickname.length() > 2) {
                                nickname = nickname.charAt(0) + "***" + nickname.charAt(nickname.length() - 1);
                        } else {
                                nickname = "***";
                        }

                        map.put("username", nickname);
                        map.put("prizeName", record.getPrizeName());
                        map.put("prizeRank", record.getPrizeRank());
                        return map;
                }).collect(Collectors.toList());

                model.addAttribute("latestWinners", latestWinners);

                // Fetch top 4 active Ichiban boxes
                // model.addAttribute("hotIchiban", ichibanBoxMapper.findAll().stream()
                // .filter(b -> b.getStatus() == IchibanBox.Status.ACTIVE)
                // .limit(4)
                // .collect(Collectors.toList()));

                // Fetch top 4 active Bingo games
                // model.addAttribute("hotBingo", bingoGameMapper.findAll().stream()
                // .filter(b -> b.getStatus() == BingoGame.Status.ACTIVE)
                // .limit(4)
                // .collect(Collectors.toList()));

                return "index";
        }

        @GetMapping("/refunds")
        public String refunds() {
                return "refunds";
        }
}
