package com.toy.store.aspect;

import com.toy.store.model.MemberActionLog;
import com.toy.store.mapper.MemberActionLogMapper;
import com.toy.store.security.services.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class MemberActionLogAspect {

    private final MemberActionLogMapper logMapper;

    @Pointcut("execution(* com.toy.store.controller.*.*(..)) && !execution(* com.toy.store.controller.AdminController.*(..))")
    public void memberControllerMethods() {
    }

    @AfterReturning("memberControllerMethods()")
    public void logMemberAction(JoinPoint joinPoint) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl user) {
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                        .currentRequestAttributes()).getRequest();
                String uri = request.getRequestURI();
                String method = request.getMethod();

                if (uri.startsWith("/css") || uri.startsWith("/js") || uri.startsWith("/images")) {
                    return;
                }

                MemberActionLog actionLog = new MemberActionLog(user.getId(), user.getUsername(), uri, method);
                logMapper.insert(actionLog);
            }
        } catch (Exception e) {
            log.warn("Failed to log member action: {}", e.getMessage());
        }
    }
}
