package com.toy.store.aspect;

import com.toy.store.model.MemberActionLog;
import com.toy.store.repository.MemberActionLogRepository;
import com.toy.store.security.services.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class MemberActionLogAspect {

    @Autowired
    private MemberActionLogRepository logRepository;

    // Intercept all Controller methods in the controller package
    // Exclude AdminController to avoid double logging or just log everything
    // User said "Frontend Member Behavior", so let's target specific controllers or
    // all excluding Admin
    @Pointcut("execution(* com.toy.store.controller.*.*(..)) && !execution(* com.toy.store.controller.AdminController.*(..))")
    public void memberControllerMethods() {
    }

    @AfterReturning("memberControllerMethods()")
    public void logMemberAction(JoinPoint joinPoint) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            // Only log if user is authenticated (Member behavior)
            if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl) {
                UserDetailsImpl user = (UserDetailsImpl) auth.getPrincipal();

                // Skip if user is ADMIN acting on frontend? Or log it?
                // User said "Member behavior", usually implies ROLE_USER, but ADMINs are
                // members too.
                // Let's log all authenticated users on frontend controllers.

                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                        .currentRequestAttributes()).getRequest();
                String uri = request.getRequestURI();
                String method = request.getMethod();

                // Filter out static resources if they somehow get here (usually handled by
                // ResourceHandler, not Controller)
                // But just in case
                if (uri.startsWith("/css") || uri.startsWith("/js") || uri.startsWith("/images")) {
                    return;
                }

                MemberActionLog log = new MemberActionLog(user.getId(), user.getUsername(), uri, method);
                logRepository.save(log);
            }
        } catch (Exception e) {
            // Silent fail
            e.printStackTrace();
        }
    }
}
