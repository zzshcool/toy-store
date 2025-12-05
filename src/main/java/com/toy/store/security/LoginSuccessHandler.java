package com.toy.store.security;

import com.toy.store.model.Member;
import com.toy.store.repository.MemberRepository;
import com.toy.store.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class LoginSuccessHandler implements ApplicationListener<AuthenticationSuccessEvent> {

    @Autowired
    private MemberRepository memberRepository;

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        Object principal = event.getAuthentication().getPrincipal();

        if (principal instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) principal;
            Member member = memberRepository.findById(userDetails.getId()).orElse(null);
            if (member != null) {
                member.setLastLoginTime(LocalDateTime.now());
                memberRepository.save(member);
            }
        }
    }
}
