package com.toy.store.controller;

import com.toy.store.model.Member;
import com.toy.store.repository.MemberRepository;
import com.toy.store.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private MemberRepository memberRepository;

    @ModelAttribute("currentUser")
    public Member getCurrentUser(@AuthenticationPrincipal Object principal) {
        if (principal instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) principal;
            return memberRepository.findById(userDetails.getId()).orElse(null);
        }
        return null;
    }
}
