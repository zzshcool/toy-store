package com.toy.store.controller;

import com.toy.store.dto.SignupRequest;
import com.toy.store.model.Member;
import com.toy.store.repository.MemberRepository;
import com.toy.store.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
public class MemberController {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PasswordEncoder encoder;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("signupRequest", new SignupRequest());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("signupRequest") SignupRequest signUpRequest,
            BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        if (memberRepository.existsByUsername(signUpRequest.getUsername())) {
            model.addAttribute("error", "錯誤: 用戶名已存在！");
            return "register";
        }

        if (memberRepository.existsByEmail(signUpRequest.getEmail())) {
            model.addAttribute("error", "錯誤: Email 已被使用！");
            return "register";
        }

        // Create new user's account
        Member member = new Member();
        member.setUsername(signUpRequest.getUsername());
        member.setEmail(signUpRequest.getEmail());
        member.setPassword(encoder.encode(signUpRequest.getPassword()));
        member.setRole(Member.Role.USER);
        member.setPlatformWalletBalance(java.math.BigDecimal.ZERO);

        // Set default nickname to username
        member.setNickname(signUpRequest.getUsername());

        memberRepository.save(member);

        return "redirect:/login";
    }

    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
        if (userDetails == null)
            return "redirect:/login";
        Member member = memberRepository.findById(userDetails.getId()).orElse(null);
        if (member == null)
            return "redirect:/login";

        model.addAttribute("member", member);
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam String nickname,
            @RequestParam String email,
            @RequestParam String phone,
            Model model) {
        Member member = memberRepository.findById(userDetails.getId()).orElse(null);
        if (member != null) {
            member.setNickname(nickname);
            member.setEmail(email);
            member.setPhone(phone);
            memberRepository.save(member);
            model.addAttribute("success", "個人資料已更新");
            model.addAttribute("member", member);
        }
        return "profile";
    }

    @GetMapping("/topup")
    public String topupPage(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
        Member member = memberRepository.findById(userDetails.getId()).orElse(null);
        model.addAttribute("member", member);
        return "topup";
    }

    @PostMapping("/topup")
    public String processTopup(@AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam java.math.BigDecimal amount,
            @RequestParam String paymentMethod,
            Model model) {
        Member member = memberRepository.findById(userDetails.getId()).orElse(null);
        if (member != null) {
            member.setPlatformWalletBalance(member.getPlatformWalletBalance().add(amount));
            memberRepository.save(member);
            model.addAttribute("success", "儲值成功！(" + paymentMethod + ")");
            model.addAttribute("member", member);
        }
        return "topup";
    }
}
