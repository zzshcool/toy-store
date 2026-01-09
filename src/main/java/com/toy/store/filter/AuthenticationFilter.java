package com.toy.store.filter;

import com.toy.store.service.TokenService;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements Filter {

    private final TokenService tokenService;

    private static final Map<String, String> PATH_PERMISSION_MAP = new HashMap<>();

    static {
        PATH_PERMISSION_MAP.put("/admin/overview", "DASHBOARD_VIEW");
        PATH_PERMISSION_MAP.put("/admin/mystery", "GACHA_MANAGE");
        PATH_PERMISSION_MAP.put("/admin/gacha", "GACHA_MANAGE");
        PATH_PERMISSION_MAP.put("/admin/ichiban", "GACHA_MANAGE");
        PATH_PERMISSION_MAP.put("/admin/bingo", "GACHA_MANAGE");
        PATH_PERMISSION_MAP.put("/admin/roulette", "GACHA_MANAGE");
        PATH_PERMISSION_MAP.put("/admin/products", "PRODUCT_MANAGE");
        PATH_PERMISSION_MAP.put("/admin/categories", "PRODUCT_MANAGE");
        PATH_PERMISSION_MAP.put("/admin/members", "MEMBER_MANAGE");
        PATH_PERMISSION_MAP.put("/admin/levels", "MEMBER_MANAGE");
        PATH_PERMISSION_MAP.put("/admin/coupons", "FINANCE_MANAGE");
        PATH_PERMISSION_MAP.put("/admin/transactions", "FINANCE_MANAGE");
        PATH_PERMISSION_MAP.put("/admin/settings", "SYSTEM_SETTING");
        PATH_PERMISSION_MAP.put("/admin/audit", "ADMIN_MANAGE");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getRequestURI();

        String memberToken = getCookieValue(httpRequest, "AUTH_TOKEN");
        String adminToken = getCookieValue(httpRequest, "ADMIN_TOKEN");

        TokenService.TokenInfo memberInfo = null;
        if (memberToken != null) {
            memberInfo = tokenService.validateMemberToken(memberToken);
            if (memberInfo != null) {
                request.setAttribute("memberTokenInfo", memberInfo);
                request.setAttribute("authenticatedUserToken", memberInfo);
            }
        }

        TokenService.TokenInfo adminInfo = null;
        if (adminToken != null) {
            adminInfo = tokenService.validateAdminToken(adminToken);
            if (adminInfo != null) {
                request.setAttribute("adminTokenInfo", adminInfo);
                if (path.startsWith("/admin")) {
                    request.setAttribute("authenticatedUserToken", adminInfo);
                }
            }
        }

        if (path.startsWith("/admin")) {
            if (path.equals("/admin/login") || path.equals("/admin/login-submit") || path.startsWith("/admin/css")
                    || path.startsWith("/admin/js")) {
                chain.doFilter(request, response);
                return;
            }

            if (adminInfo == null || !TokenService.ROLE_ADMIN.equals(adminInfo.getRole())) {
                httpResponse.sendRedirect("/admin/login");
                return;
            }

            String requiredPermission = PATH_PERMISSION_MAP.entrySet().stream()
                    .filter(entry -> path.startsWith(entry.getKey()))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);

            if (requiredPermission != null && !adminInfo.hasPermission(requiredPermission)) {
                if (path.contains("/api/")) {
                    httpResponse.setStatus(403);
                    httpResponse.setContentType("application/json;charset=UTF-8");
                    httpResponse.getWriter()
                            .write("{\"success\":false, \"message\":\"無此權限 (" + requiredPermission + ")\"}");
                } else {
                    httpResponse.sendRedirect("/admin?error=no_permission");
                }
                return;
            }
        }

        // 需要會員登入的路徑
        // 需要會員登入的路徑
        boolean isProtected = false;
        String method = httpRequest.getMethod();

        // 1. 全域保護路徑 (不分 Method)
        if (path.startsWith("/cart") || path.startsWith("/profile") || path.startsWith("/orders")
                || path.equals("/mystery-box/draw")
                || path.startsWith("/api/shard/redeem")
                || path.startsWith("/api/cabinet")
                || path.startsWith("/api/member/balance")) {
            isProtected = true;
        }
        // 2. 關鍵字保護 (僅限 POST/PUT/DELETE 等非 GET 操作，或明確的敏感路徑)
        else if (path.contains("/purchase") || path.contains("/spin") || path.contains("/dig")
                || path.contains("/lock")) {
            // 如果是這些關鍵字，通常是操作請求，需要保護
            isProtected = true;
        }
        // 3. API 特定保護 (更精細的控制)
        else if (path.startsWith("/api/blindbox/") && (path.endsWith("/draw") || path.contains("/use-"))) {
            isProtected = true;
        }

        // 4. 明確豁免 (Whitelist) - 確保遊戲列表頁面 (GET) 絕對公開
        if ("GET".equalsIgnoreCase(method)) {
            if (path.equals("/api/blindbox") || path.equals("/api/bingo") || path.equals("/api/roulette")
                    || path.equals("/api/redeem-shop")) {
                isProtected = false;
            }
            // 允許查詢單個遊戲詳情
            if (path.matches("/api/blindbox/\\d+") || path.matches("/api/bingo/\\d+")
                    || path.matches("/api/roulette/\\d+")) {
                isProtected = false;
            }
        }

        if (isProtected) {
            if (memberInfo == null) {
                if (path.startsWith("/cart/api") || path.equals("/mystery-box/draw") || path.startsWith("/admin/api")) {
                    httpResponse.setStatus(401);
                    httpResponse.setContentType("application/json;charset=UTF-8");
                    httpResponse.getWriter().write("{\"success\":false, \"message\":\"請先登入\"}");
                } else {
                    httpResponse.sendRedirect("/login");
                }
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
