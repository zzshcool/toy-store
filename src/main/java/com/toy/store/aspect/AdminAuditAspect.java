package com.toy.store.aspect;

import com.toy.store.model.AdminActionLog;
import com.toy.store.mapper.AdminActionLogMapper;
import com.toy.store.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 自動審計攔截器：記錄管理員的操作動作
 */
@Aspect
@Component
@RequiredArgsConstructor
public class AdminAuditAspect {

    private final AdminActionLogMapper adminActionLogMapper;

    @Around("execution(* com.toy.store.controller.AdminController.*(..)) && !@annotation(org.springframework.web.bind.annotation.GetMapping)")
    public Object auditAdminAction(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();

        String adminUsername = "Unknown";
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof TokenService.TokenInfo info) {
                adminUsername = info.getUsername();
                break;
            }
        }

        String action = joinPoint.getSignature().getName().toUpperCase();
        String details = "Method: " + joinPoint.getSignature().toShortString();

        String params = request.getParameterMap().entrySet().stream()
                .map(e -> e.getKey() + "=" + Arrays.toString(e.getValue()))
                .collect(Collectors.joining(", "));

        Object result;
        try {
            result = joinPoint.proceed();
            saveLog(adminUsername, action, details + " | Success", params, request.getRemoteAddr());
        } catch (Throwable e) {
            saveLog(adminUsername, action, details + " | Failed: " + e.getMessage(), params, request.getRemoteAddr());
            throw e;
        }

        return result;
    }

    private void saveLog(String admin, String action, String details, String params, String ip) {
        AdminActionLog log = new AdminActionLog();
        log.setAction(action);
        log.setDetails(details + " | IP: " + ip + " | Params: " + params);
        log.setIpAddress(ip);
        log.setCreatedAt(LocalDateTime.now());
        adminActionLogMapper.insert(log);
    }
}
