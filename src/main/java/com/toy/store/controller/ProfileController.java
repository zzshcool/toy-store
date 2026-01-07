package com.toy.store.controller;

import com.toy.store.annotation.CurrentUser;
import com.toy.store.exception.AppException;
import com.toy.store.model.Member;
import com.toy.store.model.MemberLevel;
import com.toy.store.mapper.MemberLevelMapper;
import com.toy.store.mapper.MemberMapper;
import com.toy.store.service.CouponService;
import com.toy.store.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final MemberMapper memberMapper;
    private final MemberLevelMapper memberLevelMapper;
    private final CouponService couponService;

    @GetMapping("/profile")
    public String profile(@CurrentUser TokenService.TokenInfo info, Model model) {
        if (info == null) {
            return "redirect:/login";
        }
        Member member = memberMapper.findByUsername(info.getUsername())
                .orElseThrow(() -> new AppException("找不到會員資料"));
        model.addAttribute("member", member);
        model.addAttribute("coupons", couponService.getMemberCoupons(member.getId()));

        // 等級資料
        List<MemberLevel> levels = memberLevelMapper.findAllByOrderBySortOrderAsc();
        model.addAttribute("levels", levels);

        // 計算下一等級（基於 threshold）
        BigDecimal currentSpent = member.getMonthlyRecharge() != null ? member.getMonthlyRecharge()
                : BigDecimal.ZERO;
        MemberLevel nextLevel = levels.stream()
                .filter(l -> l.getThreshold() != null && l.getThreshold().compareTo(currentSpent) > 0)
                .findFirst()
                .orElse(null);
        model.addAttribute("nextLevel", nextLevel);
        model.addAttribute("currentSpent", currentSpent);

        return "profile";
    }
}
