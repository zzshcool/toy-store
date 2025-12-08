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

    @GetMapping("/")
    public String home(Model model, jakarta.servlet.http.HttpServletRequest request) {
        // Log Action (if user is logged in)
        com.toy.store.service.TokenService.TokenInfo info = (com.toy.store.service.TokenService.TokenInfo) request
                .getAttribute("currentUser");

        if (info != null && com.toy.store.service.TokenService.ROLE_USER.equals(info.getRole())) {
            com.toy.store.model.Member member = memberRepository.findByUsername(info.getUsername())
                    .orElse(null);
            if (member != null) {
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

        return "index";
    }

    @GetMapping("/refunds")
    public String refunds() {
        return "refunds";
    }
}
