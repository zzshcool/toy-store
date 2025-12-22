package com.toy.store.controller;

import com.toy.store.model.Member;
import com.toy.store.repository.MemberRepository;
import com.toy.store.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private MemberRepository memberRepository;

    @ModelAttribute("currentUser")
    public Member getCurrentUser(HttpServletRequest request) {
        TokenService.TokenInfo info = (TokenService.TokenInfo) request.getAttribute("memberTokenInfo");
        if (info != null && TokenService.ROLE_USER.equals(info.getRole())) {
            return memberRepository.findByUsername(info.getUsername()).orElse(null);
        }
        return null;
    }

    @ModelAttribute("adminName")
    public String getAdminName(HttpServletRequest request) {
        TokenService.TokenInfo info = (TokenService.TokenInfo) request.getAttribute("adminTokenInfo");
        if (info != null && TokenService.ROLE_ADMIN.equals(info.getRole())) {
            return info.getUsername();
        }
        return "Admin";
    }
}
