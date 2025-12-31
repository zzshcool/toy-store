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

        boolean isProtected = path.startsWith("/cart") || path.startsWith("/profile") || path.startsWith("/orders")
                || path.equals("/mystery-box/draw")
                || path.contains("/purchase") || path.contains("/spin") || path.contains("/dig");

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
