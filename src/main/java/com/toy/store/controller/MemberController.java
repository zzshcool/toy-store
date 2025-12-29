package com.toy.store.controller;

import com.toy.store.dto.ApiResponse;
import com.toy.store.dto.SignupRequest;
import com.toy.store.exception.AppException;
import com.toy.store.model.Member;
import com.toy.store.repository.MemberRepository;
import com.toy.store.service.MemberService;
import com.toy.store.service.TokenService;
import com.toy.store.annotation.CurrentUser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 會員控制器
 * 僅處理路由請求與頁面導向，業務邏輯委託給 MemberService
 */
@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final TokenService tokenService;

    @GetMapping("/login")
    public String loginPage(@CurrentUser TokenService.TokenInfo info) {
        if (info != null) {
            return "redirect:/profile";
        }
        return "login";
    }

    @PostMapping("/login")
    public String loginSubmit(@RequestParam String username,
            @RequestParam String password,
            @RequestParam String captcha,
            HttpServletResponse response,
            HttpSession session) {
        try {
            memberService.login(username, password, captcha, session, response);
            return "redirect:/";
        } catch (AppException e) {
            return "redirect:/login?error&message=" + e.getMessage();
        }
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

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("signupRequest") SignupRequest signUpRequest,
            BindingResult bindingResult,
            Model model,
            HttpSession session) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            memberService.register(signUpRequest, session);
            return "redirect:/login";
        } catch (AppException e) {
            model.addAttribute("error", "錯誤: " + e.getMessage());
            return "register";
        }
    }

    @PostMapping("/profile/update")
    public String updateProfile(HttpServletRequest request,
            @RequestParam String nickname,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String realName,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate birthday,
            RedirectAttributes redirectAttributes) {
        TokenService.TokenInfo user = (TokenService.TokenInfo) request.getAttribute("authenticatedUserToken");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            memberService.updateProfile(user.getUsername(), nickname, email, phone, realName, address, gender,
                    birthday);
            redirectAttributes.addFlashAttribute("success", "資料更新成功！");
        } catch (AppException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile";
    }

    @GetMapping("/topup")
    public String topupPage(HttpServletRequest request, Model model) {
        TokenService.TokenInfo user = (TokenService.TokenInfo) request.getAttribute("authenticatedUserToken");
        if (user == null)
            return "redirect:/login";

        memberRepository.findByUsername(user.getUsername()).ifPresent(member -> model.addAttribute("member", member));
        return "topup";
    }

    @PostMapping("/topup")
    public String processTopup(HttpServletRequest request,
            @RequestParam BigDecimal amount,
            @RequestParam String paymentMethod,
            Model model) {
        TokenService.TokenInfo user = (TokenService.TokenInfo) request.getAttribute("authenticatedUserToken");
        if (user == null)
            return "redirect:/login";

        try {
            Member updatedMember = memberService.topup(user.getUsername(), amount, paymentMethod);
            model.addAttribute("success", "儲值成功！(" + paymentMethod + ")");
            model.addAttribute("member", updatedMember);
        } catch (AppException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "topup";
    }

    @PostMapping("/api/register/send-code")
    @ResponseBody
    public ApiResponse<Void> sendVerifyCode(@RequestParam String email, HttpSession session) {
        try {
            memberService.sendVerifyCode(email, session);
            return ApiResponse.ok(null, "驗證碼已發送至您的 Email");
        } catch (AppException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/api/register/verify-code")
    @ResponseBody
    public ApiResponse<Boolean> verifyCode(@RequestParam String email,
            @RequestParam String code,
            HttpSession session) {
        boolean isValid = memberService.verifyCode(email, code, session);
        if (isValid) {
            return ApiResponse.ok(true, "Email 驗證成功！");
        } else {
            return ApiResponse.error("驗證碼錯誤或已過期");
        }
    }
}
