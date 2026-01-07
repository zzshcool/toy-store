package com.toy.store.controller;

import com.toy.store.model.Member;
import com.toy.store.mapper.MemberMapper;
import com.toy.store.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final MemberMapper memberMapper;

    @ModelAttribute("currentUser")
    public Member getCurrentUser(HttpServletRequest request) {
        TokenService.TokenInfo info = (TokenService.TokenInfo) request.getAttribute("memberTokenInfo");
        if (info != null && TokenService.ROLE_USER.equals(info.getRole())) {
            return memberMapper.findByUsername(info.getUsername()).orElse(null);
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
