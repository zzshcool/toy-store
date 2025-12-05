package com.toy.store.controller;

import com.toy.store.dto.ActivityDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Controller
public class HomeController {

        @org.springframework.beans.factory.annotation.Autowired
        private com.toy.store.repository.ActivityRepository activityRepository;

        @org.springframework.beans.factory.annotation.Autowired
        private com.toy.store.repository.MemberActionLogRepository memberActionLogRepository;

        @GetMapping("/")
        public String home(Model model) {
                // Log Action (if user is logged in)
                org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                                .getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated()
                                && auth.getPrincipal() instanceof com.toy.store.security.services.UserDetailsImpl) {
                        com.toy.store.security.services.UserDetailsImpl user = (com.toy.store.security.services.UserDetailsImpl) auth
                                        .getPrincipal();
                        memberActionLogRepository.save(new com.toy.store.model.MemberActionLog(
                                        user.getId(), user.getUsername(), "VIEW_HOME", "Viewed Active Activities",
                                        true));
                }

                // Fetch active activities from DB
                List<com.toy.store.model.Activity> activities = activityRepository.findAll().stream()
                                .filter(a -> a.isActive())
                                .collect(java.util.stream.Collectors.toList());

                model.addAttribute("activities", activities);
                return "index";
        }
}
