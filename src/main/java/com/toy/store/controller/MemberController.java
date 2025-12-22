package com.toy.store.controller;

import com.toy.store.dto.SignupRequest;
import com.toy.store.model.Member;
import com.toy.store.model.Transaction;
import com.toy.store.repository.MemberRepository;
import com.toy.store.service.EmailVerificationService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @Autowired
    private EmailVerificationService emailVerificationService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String loginSubmit(@RequestParam String username,
            @RequestParam String password,
            @RequestParam String captcha,
            HttpServletResponse response,
            Model model,
            jakarta.servlet.http.HttpSession session) {

        // 驗證碼校驗
        String sessionCaptcha = (String) session.getAttribute("captcha");
        if (sessionCaptcha == null || !sessionCaptcha.equalsIgnoreCase(captcha)) {
            return "redirect:/login?captcha_error";
        }

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
                    tokenService.invalidateMemberToken(cookie.getValue());
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
            BindingResult bindingResult, Model model, jakarta.servlet.http.HttpSession session) {
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

        // 驗證碼校驗
        String sessionCaptcha = (String) session.getAttribute("captcha");
        if (sessionCaptcha == null || !sessionCaptcha.equalsIgnoreCase(signUpRequest.getCaptcha())) {
            model.addAttribute("error", "錯誤: 驗證碼不正確！");
            return "register";
        }

        // 條款同意校驗
        if (!signUpRequest.isAgreedTerms()) {
            model.addAttribute("error", "錯誤: 您必須同意會員條款與隱私權政策才能註冊！");
            return "register";
        }

        // Email 驗證狀態校驗
        if (!emailVerificationService.isEmailVerified(signUpRequest.getEmail(), session)) {
            model.addAttribute("error", "錯誤: 您的 Email 尚未通過驗證！");
            return "register";
        }

        // Create new user's account
        Member member = new Member();
        member.setUsername(signUpRequest.getUsername());
        member.setEmail(signUpRequest.getEmail());
        member.setPhone(signUpRequest.getPhone());
        member.setRealName(signUpRequest.getRealName());
        member.setAddress(signUpRequest.getAddress());
        member.setGender(signUpRequest.getGender());
        member.setBirthday(signUpRequest.getBirthday());
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
            @RequestParam String realName,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate birthday,
            RedirectAttributes redirectAttributes) {
        TokenService.TokenInfo user = (TokenService.TokenInfo) request.getAttribute("authenticatedUserToken");
        if (user == null) {
            return "redirect:/login";
        }

        Member member = memberRepository.findByUsername(user.getUsername()).orElse(null);
        if (member != null) {
            member.setNickname(nickname);
            member.setEmail(email);
            member.setPhone(phone);
            member.setRealName(realName);
            member.setAddress(address);
            member.setGender(gender);
            member.setBirthday(birthday);
            memberRepository.save(member);
            redirectAttributes.addFlashAttribute("success", "資料更新成功！");
        }
        return "redirect:/profile";
    }

    @GetMapping("/topup")
    public String topupPage(HttpServletRequest request, Model model) {
        TokenService.TokenInfo user = (TokenService.TokenInfo) request.getAttribute("authenticatedUserToken");
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
        TokenService.TokenInfo user = (TokenService.TokenInfo) request.getAttribute("authenticatedUserToken");
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
            Long memberId = java.util.Objects.requireNonNull(member.getId());
            member = memberRepository.findById(memberId).orElseThrow();
            model.addAttribute("success", "儲值成功！(" + paymentMethod + ")");
            model.addAttribute("member", member);
        }
        return "topup";
    }

    @PostMapping("/api/register/send-code")
    @ResponseBody
    public java.util.Map<String, Object> sendVerifyCode(@RequestParam String email,
            jakarta.servlet.http.HttpSession session) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();

        if (memberRepository.existsByEmail(email)) {
            response.put("success", false);
            response.put("message", "此 Email 已被註冊！");
            return response;
        }

        emailVerificationService.generateAndSaveCode(email, session);
        response.put("success", true);
        response.put("message", "驗證碼已發送至您的 Email (請查看後台日誌)");
        return response;
    }

    @PostMapping("/api/register/verify-code")
    @ResponseBody
    public java.util.Map<String, Object> verifyCode(@RequestParam String email, @RequestParam String code,
            jakarta.servlet.http.HttpSession session) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        boolean isValid = emailVerificationService.verifyCode(email, code, session);

        response.put("success", isValid);
        response.put("message", isValid ? "Email 驗證成功！" : "驗證碼錯誤或已過期");
        return response;
    }
}
