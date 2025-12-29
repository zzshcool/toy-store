package com.toy.store.aspect;

import com.toy.store.model.AdminActionLog;
import com.toy.store.repository.AdminActionLogRepository;
import com.toy.store.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 自動審計攔截器：記錄管理員的操作動作
 */
@Aspect
@Component
public class AdminAuditAspect {

    @Autowired
    private AdminActionLogRepository adminActionLogRepository;

    // 攔截 AdminController 中所有非 GET 的方法 (修改類動作)
    @Around("execution(* com.toy.store.controller.AdminController.*(..)) && !@annotation(org.springframework.web.bind.annotation.GetMapping)")
    public Object auditAdminAction(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();

        // 獲取當前操作人資訊 (從方法參數中尋找 @CurrentUser)
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

        // 捕捉請求參數
        String params = request.getParameterMap().entrySet().stream()
                .map(e -> e.getKey() + "=" + Arrays.toString(e.getValue()))
                .collect(Collectors.joining(", "));

        Object result;
        try {
            result = joinPoint.proceed();

            // 成功執行
            saveLog(adminUsername, action, details + " | Success", params, request.getRemoteAddr());

        } catch (Throwable e) {
            // 執行失敗
            saveLog(adminUsername, action, details + " | Failed: " + e.getMessage(), params, request.getRemoteAddr());
            throw e;
        }

        return result;
    }

    private void saveLog(String admin, String action, String details, String params, String ip) {
        AdminActionLog log = new AdminActionLog(admin, action, details, "IP: " + ip + " | Params: " + params);
        adminActionLogRepository.save(log);
    }
}
