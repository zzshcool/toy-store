package com.toy.store.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

        @org.springframework.beans.factory.annotation.Autowired
        private com.toy.store.repository.ActivityRepository activityRepository;

        @org.springframework.beans.factory.annotation.Autowired
        private com.toy.store.repository.MemberActionLogRepository memberActionLogRepository;

        @org.springframework.beans.factory.annotation.Autowired
        private com.toy.store.repository.MemberRepository memberRepository;

        @org.springframework.beans.factory.annotation.Autowired
        private com.toy.store.repository.CarouselSlideRepository carouselSlideRepository;

        @org.springframework.beans.factory.annotation.Autowired
        private com.toy.store.repository.FeaturedItemRepository featuredItemRepository;

        @org.springframework.beans.factory.annotation.Autowired
        private com.toy.store.repository.GachaRecordRepository gachaRecordRepository;

        @org.springframework.beans.factory.annotation.Autowired
        private com.toy.store.repository.IchibanBoxRepository ichibanBoxRepository;

        @org.springframework.beans.factory.annotation.Autowired
        private com.toy.store.repository.BingoGameRepository bingoGameRepository;

        @GetMapping("/")
        public String home(Model model, jakarta.servlet.http.HttpServletRequest request) {
                // Log Action (if user is logged in)
                com.toy.store.service.TokenService.TokenInfo info = (com.toy.store.service.TokenService.TokenInfo) request
                                .getAttribute("currentUser");

                if (info != null && com.toy.store.service.TokenService.ROLE_USER.equals(info.getRole())) {
                        com.toy.store.model.Member member = memberRepository.findByUsername(info.getUsername())
                                        .orElse(null);
                        if (member != null) {
                                model.addAttribute("currentMember", member);
                                memberActionLogRepository.save(new com.toy.store.model.MemberActionLog(
                                                member.getId(), member.getUsername(), "VIEW_HOME",
                                                "Viewed Active Activities",
                                                true));
                        }
                }

                // Fetch active activities from DB
                List<com.toy.store.model.Activity> activities = activityRepository.findAll().stream()
                                .filter(a -> a.isActive())
                                .collect(java.util.stream.Collectors.toList());

                model.addAttribute("activities", activities);

                // Platform Data
                model.addAttribute("carouselSlides", carouselSlideRepository.findAllByOrderBySortOrderAsc());
                model.addAttribute("newArrivals", featuredItemRepository
                                .findByItemTypeOrderBySortOrderAsc(com.toy.store.model.FeaturedItem.Type.NEW_ARRIVAL));
                model.addAttribute("bestSellers", featuredItemRepository
                                .findByItemTypeOrderBySortOrderAsc(com.toy.store.model.FeaturedItem.Type.BEST_SELLER));

                // Commercial Optimization Data
                List<com.toy.store.model.GachaRecord> records = gachaRecordRepository.findTop20ByOrderByCreatedAtDesc();
                List<java.util.Map<String, Object>> latestWinners = records.stream().map(record -> {
                        java.util.Map<String, Object> map = new java.util.HashMap<>();
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
                }).collect(java.util.stream.Collectors.toList());

                model.addAttribute("latestWinners", latestWinners);

                // Fetch top 4 active Ichiban boxes
                model.addAttribute("hotIchiban", ichibanBoxRepository.findAll().stream()
                                .filter(b -> b.getStatus() == com.toy.store.model.IchibanBox.Status.ACTIVE)
                                .limit(4)
                                .collect(java.util.stream.Collectors.toList()));

                // Fetch top 4 active Bingo games
                model.addAttribute("hotBingo", bingoGameRepository.findAll().stream()
                                .filter(b -> b.getStatus() == com.toy.store.model.BingoGame.Status.ACTIVE)
                                .limit(4)
                                .collect(java.util.stream.Collectors.toList()));

                return "index";
        }

        @GetMapping("/refunds")
        public String refunds() {
                return "refunds";
        }
}
