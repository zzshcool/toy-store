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
        TokenService.TokenInfo info = (TokenService.TokenInfo) request.getAttribute("currentUser");
        if (info != null && TokenService.ROLE_USER.equals(info.getRole())) {
            return memberRepository.findByUsername(info.getUsername()).orElse(null);
        }
        return null; // Return null if not logged in or is admin (unless admin also needs this member
                     // obj?)
        // If Templates expect 'currentUser' to be null when not logged in, this is
        // fine.
    }
}
