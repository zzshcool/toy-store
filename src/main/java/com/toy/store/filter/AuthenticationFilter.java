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
import java.util.Arrays;
import java.util.List;

@Component
public class AuthenticationFilter implements Filter {

    @Autowired
    private TokenService tokenService;

    // Public paths that don't satisfy authentication
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/", "/login", "/register", "/products", "/mystery-box",
            "/css/", "/js/", "/images/", "/error", "/h2-console", "/cart/api");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getRequestURI();

        // 1. Static resources and public endpoints pass through
        // Check if path starts with any public path (simple prefix match)
        boolean isPublic = PUBLIC_PATHS.stream().anyMatch(
                p -> path.equals(p) || path.startsWith(p + "/") || path.startsWith("/css") || path.startsWith("/js"));

        // Special case: /mystery-box/draw needs auth, so we should be careful what we
        // exclude.
        // /mystery-box is public, but let's check for specific protected actions later?
        // Actually, let's keep it simple: Protected routes are Explicitly Checked or we
        // check "if not public".

        // Check for specific cookies based on path
        String token = null;
        String cookieName = path.startsWith("/admin") ? "ADMIN_TOKEN" : "AUTH_TOKEN";

        Cookie[] cookies = httpRequest.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        TokenService.TokenInfo tokenInfo = null;
        if (token != null) {
            tokenInfo = tokenService.validateToken(token);
        }

        request.setAttribute("currentUser", tokenInfo); // Set currentUser if logged in

        // 2. Admin Routes Protection
        if (path.startsWith("/admin")) {
            // Allow login page resources
            if (path.equals("/admin/login") || path.equals("/admin/login-submit") || path.startsWith("/admin/css")
                    || path.startsWith("/admin/js")) {
                chain.doFilter(request, response);
                return;
            }

            if (tokenInfo == null || !TokenService.ROLE_ADMIN.equals(tokenInfo.getRole())) {
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
            if (tokenInfo == null) {
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
}
