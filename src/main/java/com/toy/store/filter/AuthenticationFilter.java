package com.toy.store.filter;

import com.toy.store.service.TokenService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthenticationFilter implements Filter {

    @Autowired
    private TokenService tokenService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getRequestURI();

        // 1. Static resources and public endpoints pass through
        // Check if path starts with any public path (simple prefix match)

        // Special case: /mystery-box/draw needs auth, so we should be careful what we
        // exclude.
        // /mystery-box is public, but let's check for specific protected actions later?
        // Actually, let's keep it simple: Protected routes are Explicitly Checked or we
        // check "if not public".

        // 1. Token Validation
        String memberToken = getCookieValue(httpRequest, "AUTH_TOKEN");
        String adminToken = getCookieValue(httpRequest, "ADMIN_TOKEN");

        TokenService.TokenInfo memberInfo = null;
        if (memberToken != null) {
            memberInfo = tokenService.validateMemberToken(memberToken);
            if (memberInfo != null) {
                request.setAttribute("memberTokenInfo", memberInfo);
                request.setAttribute("authenticatedUserToken", memberInfo); // 保持後向相容
            }
        }

        TokenService.TokenInfo adminInfo = null;
        if (adminToken != null) {
            adminInfo = tokenService.validateAdminToken(adminToken);
            if (adminInfo != null) {
                request.setAttribute("adminTokenInfo", adminInfo);
                // 如果是管理後台，優先覆蓋相容屬性
                if (path.startsWith("/admin")) {
                    request.setAttribute("authenticatedUserToken", adminInfo);
                }
            }
        }

        // 2. Admin Routes Protection
        if (path.startsWith("/admin")) {
            // Allow login page resources
            if (path.equals("/admin/login") || path.equals("/admin/login-submit") || path.startsWith("/admin/css")
                    || path.startsWith("/admin/js")) {
                chain.doFilter(request, response);
                return;
            }

            if (adminInfo == null || !TokenService.ROLE_ADMIN.equals(adminInfo.getRole())) {
                httpResponse.sendRedirect("/admin/login");
                return;
            }
        }

        // 3. Protected Member Routes
        // If it's NOT a public path and NOT an admin path, assume it needs USER auth?
        // Or specific routes?
        // Let's define Protected Routes: /cart, /profile, /orders, /mystery-box/draw

        boolean isProtected = path.startsWith("/cart") || path.startsWith("/profile") || path.startsWith("/orders")
                || path.equals("/mystery-box/draw");

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
