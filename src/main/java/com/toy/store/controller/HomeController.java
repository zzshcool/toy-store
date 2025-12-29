package com.toy.store.controller;

import com.toy.store.model.*;
import com.toy.store.repository.*;
import com.toy.store.service.MissionService;
import com.toy.store.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class HomeController {

        private final ActivityRepository activityRepository;
        private final MemberActionLogRepository memberActionLogRepository;
        private final MemberRepository memberRepository;
        private final CarouselSlideRepository carouselSlideRepository;
        private final FeaturedItemRepository featuredItemRepository;
        private final GachaRecordRepository gachaRecordRepository;
        private final IchibanBoxRepository ichibanBoxRepository;
        private final BingoGameRepository bingoGameRepository;
        private final MissionService missionService;
        private final MemberMissionRepository memberMissionRepository;
        private final MemberSignInRepository memberSignInRepository;

        @GetMapping("/")
        public String home(Model model, HttpServletRequest request) {
                // Log Action (if user is logged in)
                TokenService.TokenInfo info = (TokenService.TokenInfo) request.getAttribute("currentUser");

                if (info != null && TokenService.ROLE_USER.equals(info.getRole())) {
                        memberRepository.findByUsername(info.getUsername()).ifPresent(member -> {
                                model.addAttribute("currentMember", member);

                                // 初始化今日任務並獲取進度
                                missionService.initDailyMissions(member.getId());
                                model.addAttribute("dailyMissions",
                                                memberMissionRepository.findByMemberIdAndMissionDate(member.getId(),
                                                                LocalDate.now()));

                                // 獲取今日簽到狀況
                                model.addAttribute("signInInfo",
                                                memberSignInRepository
                                                                .findByMemberIdAndSignInDate(member.getId(),
                                                                                LocalDate.now())
                                                                .orElse(null));

                                memberActionLogRepository.save(new MemberActionLog(
                                                member.getId(), member.getUsername(), "VIEW_HOME",
                                                "Viewed Active Activities", true));
                        });
                }

                // Fetch active activities from DB
                List<Activity> activities = activityRepository.findAll().stream()
                                .filter(Activity::isActive)
                                .collect(Collectors.toList());

                model.addAttribute("activities", activities);

                // Platform Data
                model.addAttribute("carouselSlides", carouselSlideRepository.findAllByOrderBySortOrderAsc());
                model.addAttribute("newArrivals", featuredItemRepository
                                .findByItemTypeOrderBySortOrderAsc(FeaturedItem.Type.NEW_ARRIVAL));
                model.addAttribute("bestSellers", featuredItemRepository
                                .findByItemTypeOrderBySortOrderAsc(FeaturedItem.Type.BEST_SELLER));

                // Commercial Optimization Data
                List<GachaRecord> records = gachaRecordRepository.findTop20ByOrderByCreatedAtDesc();
                List<Map<String, Object>> latestWinners = records.stream().map(record -> {
                        Map<String, Object> map = new HashMap<>();
                        Long memberId = record.getMemberId();
                        String nickname = (memberId != null) ? memberRepository.findById(memberId)
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
                model.addAttribute("hotIchiban", ichibanBoxRepository.findAll().stream()
                                .filter(b -> b.getStatus() == IchibanBox.Status.ACTIVE)
                                .limit(4)
                                .collect(Collectors.toList()));

                // Fetch top 4 active Bingo games
                model.addAttribute("hotBingo", bingoGameRepository.findAll().stream()
                                .filter(b -> b.getStatus() == BingoGame.Status.ACTIVE)
                                .limit(4)
                                .collect(Collectors.toList()));

                return "index";
        }

        @GetMapping("/refunds")
        public String refunds() {
                return "refunds";
        }
}
