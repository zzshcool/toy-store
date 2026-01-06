package com.toy.store.aspect;

import com.toy.store.model.AdminActionLog;
import com.toy.store.mapper.AdminActionLogMapper;
import com.toy.store.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Aspect
@Component
@RequiredArgsConstructor
public class AdminActionAspect {

        private final AdminActionLogMapper adminActionLogMapper;

        @Pointcut("within(com.toy.store.controller.AdminController) || within(com.toy.store.controller.admin..*)")
        public void adminController() {
        }

        @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
                        "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
                        "@annotation(org.springframework.web.bind.annotation.DeleteMapping) || " +
                        "@annotation(org.springframework.web.bind.annotation.PatchMapping)")
        public void writeActions() {
        }

        @AfterReturning(pointcut = "adminController() && writeActions()", returning = "result")
        public void logAdminAction(JoinPoint joinPoint, Object result) {
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                                .currentRequestAttributes())
                                .getRequest();

                TokenService.TokenInfo info = (TokenService.TokenInfo) request.getAttribute("currentUser");
                String adminName = (info != null) ? info.getUsername() : "System/Unknown";

                String action = joinPoint.getSignature().getName();
                String methodName = joinPoint.getSignature().getDeclaringTypeName() + "." + action;

                Map<String, String[]> parameterMap = request.getParameterMap();
                String params = parameterMap.entrySet().stream()
                                .map(entry -> entry.getKey() + "=" + Arrays.toString(entry.getValue()))
                                .collect(Collectors.joining("; "));

                String details = "Auto-logged: " + action;

                AdminActionLog log = new AdminActionLog();
                log.setAction(action);
                log.setDetails(details);
                log.setIpAddress(request.getRemoteAddr());
                log.setCreatedAt(LocalDateTime.now());
                adminActionLogMapper.insert(log);
        }
}
