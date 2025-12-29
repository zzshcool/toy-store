package com.toy.store.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * API 請求限流過濾器
 * 防止惡意請求和 DDoS 攻擊
 */
@Slf4j
@Component
@Order(1)
public class RateLimitFilter implements Filter {

    // IP 請求記錄（IP -> 請求計數器）
    private final Map<String, RequestCounter> ipCounters = new ConcurrentHashMap<>();

    // 每分鐘最大請求數
    private static final int MAX_REQUESTS_PER_MINUTE = 120;

    // 被封鎖的 IP 名單（IP -> 解封時間戳）
    private final Map<String, Long> blockedIps = new ConcurrentHashMap<>();

    // 封鎖時間（毫秒）
    private static final long BLOCK_DURATION_MS = 5 * 60 * 1000; // 5 分鐘

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();

        // 只對 API 請求進行限流
        if (!path.startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(httpRequest);

        // 檢查是否被封鎖
        if (isBlocked(clientIp)) {
            log.warn("IP {} 被封鎖，拒絕請求: {}", clientIp, path);
            httpResponse.setStatus(429); // Too Many Requests
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().write("{\"success\":false,\"message\":\"請求過於頻繁，請稍後再試\"}");
            return;
        }

        // 檢查請求頻率
        if (!checkRateLimit(clientIp)) {
            log.warn("IP {} 請求超限，暫時封鎖: {}", clientIp, path);
            blockIp(clientIp);
            httpResponse.setStatus(429);
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().write("{\"success\":false,\"message\":\"請求過於頻繁，請稍後再試\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    /**
     * 獲取客戶端 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    /**
     * 檢查 IP 是否被封鎖
     */
    private boolean isBlocked(String ip) {
        Long unblockTime = blockedIps.get(ip);
        if (unblockTime == null) {
            return false;
        }
        if (System.currentTimeMillis() >= unblockTime) {
            blockedIps.remove(ip);
            return false;
        }
        return true;
    }

    /**
     * 封鎖 IP
     */
    private void blockIp(String ip) {
        blockedIps.put(ip, System.currentTimeMillis() + BLOCK_DURATION_MS);
    }

    /**
     * 檢查請求頻率
     */
    private boolean checkRateLimit(String ip) {
        long now = System.currentTimeMillis();
        RequestCounter counter = ipCounters.computeIfAbsent(ip, k -> new RequestCounter());

        synchronized (counter) {
            // 重置計數器（每分鐘）
            if (now - counter.windowStart > 60000) {
                counter.windowStart = now;
                counter.count.set(0);
            }

            // 增加計數
            int currentCount = counter.count.incrementAndGet();
            return currentCount <= MAX_REQUESTS_PER_MINUTE;
        }
    }

    /**
     * 請求計數器
     */
    private static class RequestCounter {
        volatile long windowStart = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger(0);
    }

    @Override
    public void init(FilterConfig filterConfig) {
        log.info("RateLimitFilter 初始化完成，限制: {} 請求/分鐘", MAX_REQUESTS_PER_MINUTE);
    }

    @Override
    public void destroy() {
        ipCounters.clear();
        blockedIps.clear();
    }
}
