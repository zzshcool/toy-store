package com.toy.store.controller;

import com.toy.store.dto.SignupRequest;
import com.toy.store.model.Member;
import com.toy.store.model.Transaction;
import com.toy.store.repository.MemberRepository;
import com.toy.store.service.TokenService;
import com.toy.store.service.TransactionService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class MemberController {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String loginSubmit(@RequestParam String username, @RequestParam String password,
            HttpServletResponse response, Model model) {

        java.util.Optional<Member> memberOpt = memberRepository.findByUsername(username);
        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();
            if (encoder.matches(password, member.getPassword())) {
                // Success
                String token = tokenService.createToken(member.getUsername(), TokenService.ROLE_USER);

                Cookie cookie = new Cookie("AUTH_TOKEN", token);
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                cookie.setMaxAge(3600 * 24 * 7); // 7 days
                response.addCookie(cookie);

                // Update Last Login
                member.setLastLoginTime(java.time.LocalDateTime.now());
                memberRepository.save(member);

                return "redirect:/";
            }
        }

        return "redirect:/login?error";
    }

    @RequestMapping(value = "/logout", method = { RequestMethod.GET, RequestMethod.POST })
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("AUTH_TOKEN".equals(cookie.getName())) {
                    tokenService.invalidateToken(cookie.getValue());
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    cookie.setValue(null);
                    response.addCookie(cookie);
                    break;
                }
            }
        }
        return "redirect:/";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("signupRequest", new SignupRequest());
        return "register";
    }

    @Autowired
    private com.toy.store.repository.MemberLevelRepository memberLevelRepository;

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("signupRequest") SignupRequest signUpRequest,
            BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        if (memberRepository.existsByUsername(signUpRequest.getUsername())) {
            model.addAttribute("error", "錯誤: 此帳號已被註冊！");
            return "register";
        }

        if (memberRepository.existsByEmail(signUpRequest.getEmail())) {
            model.addAttribute("error", "錯誤: 此 Email 已被使用！");
            return "register";
        }

        // Create new user's account
        Member member = new Member();
        member.setUsername(signUpRequest.getUsername());
        member.setEmail(signUpRequest.getEmail());
        member.setPhone(signUpRequest.getPhone());
        member.setPassword(encoder.encode(signUpRequest.getPassword()));
        member.setRole(Member.Role.USER);
        member.setPlatformWalletBalance(java.math.BigDecimal.ZERO);

        // 設定暱稱（如果有填寫則使用填寫的，否則使用帳號名）
        String nickname = signUpRequest.getNickname();
        member.setNickname(
                nickname != null && !nickname.trim().isEmpty() ? nickname.trim() : signUpRequest.getUsername());

        // 初始化會員等級（自動指派最低等級）
        java.util.List<com.toy.store.model.MemberLevel> levels = memberLevelRepository
                .findByEnabledTrueOrderBySortOrderAsc();
        if (!levels.isEmpty()) {
            member.setLevel(levels.get(0));
        }

        memberRepository.save(member);

        return "redirect:/login";
    }

    @PostMapping("/profile/update")
    public String updateProfile(HttpServletRequest request,
            @RequestParam String nickname,
            @RequestParam String email,
            @RequestParam String phone,
            Model model) {
        TokenService.TokenInfo user = (TokenService.TokenInfo) request.getAttribute("currentUser");
        if (user == null)
            return "redirect:/login";

        Member member = memberRepository.findByUsername(user.getUsername()).orElse(null);
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
    public String topupPage(HttpServletRequest request, Model model) {
        TokenService.TokenInfo user = (TokenService.TokenInfo) request.getAttribute("currentUser");
        if (user == null)
            return "redirect:/login";

        Member member = memberRepository.findByUsername(user.getUsername()).orElse(null);
        model.addAttribute("member", member);
        return "topup";
    }

    @PostMapping("/topup")
    public String processTopup(HttpServletRequest request,
            @RequestParam java.math.BigDecimal amount,
            @RequestParam String paymentMethod,
            Model model) {
        TokenService.TokenInfo user = (TokenService.TokenInfo) request.getAttribute("currentUser");
        if (user == null)
            return "redirect:/login";

        Member member = memberRepository.findByUsername(user.getUsername()).orElse(null);
        if (member != null) {
            // 使用 TransactionService 處理儲值（會記錄交易 + 檢查升等）
            transactionService.updateWalletBalance(
                    member.getId(),
                    amount,
                    Transaction.TransactionType.DEPOSIT,
                    "TOPUP-" + paymentMethod);

            // 重新載入會員資料
            member = memberRepository.findById(member.getId()).orElseThrow();
            model.addAttribute("success", "儲值成功！(" + paymentMethod + ")");
            model.addAttribute("member", member);
        }
        return "topup";
    }
}
