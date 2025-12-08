package com.toy.store.controller;

import com.toy.store.model.Member;
import com.toy.store.service.TokenService;
import com.toy.store.repository.MemberRepository;
import com.toy.store.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ProfileController {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CouponService couponService;

    @GetMapping("/profile")
    public String profile(HttpServletRequest request, Model model) {
        TokenService.TokenInfo info = (TokenService.TokenInfo) request.getAttribute("currentUser");
        if (info == null) {
            return "redirect:/login";
        }
        Member member = memberRepository.findByUsername(info.getUsername()).orElseThrow();
        model.addAttribute("member", member);
        model.addAttribute("coupons", couponService.getMemberCoupons(member.getId()));
        return "profile";
    }
}
