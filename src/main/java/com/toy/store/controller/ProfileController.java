package com.toy.store.controller;

import com.toy.store.model.Member;
import com.toy.store.model.MemberLevel;
import com.toy.store.service.TokenService;
import com.toy.store.repository.MemberRepository;
import com.toy.store.repository.MemberLevelRepository;
import com.toy.store.service.CouponService;
import com.toy.store.annotation.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class ProfileController {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberLevelRepository memberLevelRepository;

    @Autowired
    private CouponService couponService;

    @GetMapping("/profile")
    public String profile(@CurrentUser TokenService.TokenInfo info, Model model) {
        if (info == null) {
            return "redirect:/login";
        }
        Member member = memberRepository.findByUsername(info.getUsername())
                .orElseThrow(() -> new RuntimeException("找不到會資料"));
        model.addAttribute("member", member);
        model.addAttribute("coupons", couponService.getMemberCoupons(member.getId()));

        // 等級資料
        List<MemberLevel> levels = memberLevelRepository.findAllByOrderBySortOrderAsc();
        model.addAttribute("levels", levels);

        // 計算下一等級（基於 threshold）
        java.math.BigDecimal currentSpent = member.getMonthlyRecharge() != null ? member.getMonthlyRecharge()
                : java.math.BigDecimal.ZERO;
        MemberLevel nextLevel = levels.stream()
                .filter(l -> l.getThreshold() != null && l.getThreshold().compareTo(currentSpent) > 0)
                .findFirst()
                .orElse(null);
        model.addAttribute("nextLevel", nextLevel);
        model.addAttribute("currentSpent", currentSpent);

        return "profile";
    }
}
