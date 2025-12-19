package com.toy.store.controller;

import com.toy.store.annotation.CurrentUser;
import com.toy.store.model.Product;
import com.toy.store.repository.MemberRepository;
import com.toy.store.repository.MemberLevelRepository;
import com.toy.store.service.ProductService;
import com.toy.store.service.TokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private com.toy.store.service.AdminService adminService;

    @Autowired
    private com.toy.store.repository.AdminUserRepository adminUserRepository;

    @Autowired
    private com.toy.store.repository.AdminActionLogRepository adminActionLogRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private com.toy.store.repository.CategoryRepository categoryRepository;

    @GetMapping("/login")
    public String adminLogin() {
        return "admin_login";
    }

    @PostMapping("/login-submit")
    public String loginSubmit(@RequestParam String username, @RequestParam String password,
            HttpServletResponse response, HttpServletRequest request) {

        Optional<com.toy.store.model.AdminUser> adminOpt = adminUserRepository.findByUsername(username);

        if (adminOpt.isPresent()) {
            if (passwordEncoder.matches(password, adminOpt.get().getPassword())) {
                // Success
                String token = tokenService.createToken(username, TokenService.ROLE_ADMIN);
                Cookie cookie = new Cookie("ADMIN_TOKEN", token);
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                cookie.setMaxAge(28800);
                response.addCookie(cookie);

                // Auto-logged by AOP

                return "redirect:/admin";
            }
        }

        // FAILED login is a POST, but AOP might not capture correctly if we return
        // early.
        // Let's keep manual failed log for security.
        adminActionLogRepository.save(new com.toy.store.model.AdminActionLog(
                username, "LOGIN_FAILED", "Invalid credentials", "IP: " + request.getRemoteAddr()));

        return "redirect:/admin/login?error";
    }

    @RequestMapping(value = "/logout", method = { RequestMethod.GET, RequestMethod.POST })
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("ADMIN_TOKEN".equals(cookie.getName())) {
                    tokenService.invalidateToken(cookie.getValue());
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    cookie.setValue(null);
                    response.addCookie(cookie);
                    break;
                }
            }
        }
        return "redirect:/admin/login?logout";
    }

    @GetMapping
    public String adminDashboard(Model model, @CurrentUser TokenService.TokenInfo info,
            @RequestParam(defaultValue = "overview") String tab,
            @RequestParam(defaultValue = "0") int memberPage,
            @RequestParam(defaultValue = "20") int memberSize,
            @RequestParam(defaultValue = "0") int txPage,
            @RequestParam(defaultValue = "20") int txSize,
            @RequestParam(defaultValue = "0") int logPage,
            @RequestParam(defaultValue = "20") int logSize,
            @RequestParam(defaultValue = "0") int gachaPage,
            @RequestParam(defaultValue = "10") int gachaSize) {

        model.addAttribute("activeTab", tab);

        Map<String, Object> data = adminService.getDashboardData(
                memberPage, memberSize, txPage, txSize, logPage, logSize, gachaPage, gachaSize);
        model.addAllAttributes(data);

        // Initialize new objects for forms
        model.addAttribute("newProduct", new Product());
        model.addAttribute("newTheme", new com.toy.store.model.MysteryBoxTheme());
        model.addAttribute("newActivity", new com.toy.store.model.Activity());
        model.addAttribute("newCategory", new com.toy.store.model.Category());

        return "admin";
    }

    // --- Product Management ---
    @PostMapping("/products")
    public String createProduct(@ModelAttribute Product product, @CurrentUser TokenService.TokenInfo info) {
        adminService.saveProduct(product, info != null ? info.getUsername() : "System");
        return "redirect:/admin?tab=products";
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, @CurrentUser TokenService.TokenInfo info) {
        adminService.deleteProduct(id, info != null ? info.getUsername() : "System");
        return "redirect:/admin?tab=products";
    }

    @PostMapping("/categories")
    public String createCategory(@ModelAttribute com.toy.store.model.Category category,
            @CurrentUser TokenService.TokenInfo info) {
        adminService.createCategory(category, info != null ? info.getUsername() : "System");
        return "redirect:/admin?tab=categories";
    }

    @PostMapping("/subcategories")
    public String createSubCategory(@RequestParam Long categoryId, @RequestParam String name,
            @CurrentUser TokenService.TokenInfo info) {
        adminService.createSubCategory(categoryId, name, info != null ? info.getUsername() : "System");
        return "redirect:/admin?tab=categories";
    }

    @GetMapping("/api/subcategories/{categoryId}")
    @ResponseBody
    public List<com.toy.store.model.SubCategory> getSubCategories(@PathVariable Long categoryId) {
        return categoryRepository.findById(categoryId).map(com.toy.store.model.Category::getSubCategories)
                .orElse(Collections.emptyList());
    }

    // --- Member Management ---
    @PostMapping("/members/{id}/toggle-status")
    public String toggleMemberStatus(@PathVariable Long id, @CurrentUser TokenService.TokenInfo info) {
        adminService.toggleMemberStatus(id, info != null ? info.getUsername() : "System");
        return "redirect:/admin?tab=members";
    }

    @GetMapping("/api/members/{id}/history")
    @ResponseBody
    public Map<String, Object> getMemberHistory(@PathVariable Long id) {
        return adminService.getMemberHistory(id);
    }

    // --- Mystery Box Management ---
    @PostMapping("/mystery-box/themes")
    public String createTheme(@ModelAttribute com.toy.store.model.MysteryBoxTheme theme,
            @CurrentUser TokenService.TokenInfo info) {
        adminService.createTheme(theme, info != null ? info.getUsername() : "System");
        return "redirect:/admin?tab=mystery";
    }

    @PostMapping("/mystery-box/items")
    public String createMysteryBoxItem(@RequestParam Long themeId,
            @ModelAttribute com.toy.store.model.MysteryBoxItem item,
            @CurrentUser TokenService.TokenInfo info) {
        adminService.createMysteryBoxItem(themeId, item, info != null ? info.getUsername() : "System");
        return "redirect:/admin?tab=mystery";
    }

    // --- Activity Management ---
    @PostMapping("/activities/delete/{id}")
    public String deleteActivity(@PathVariable Long id, @CurrentUser TokenService.TokenInfo info) {
        adminService.deleteActivity(id, info != null ? info.getUsername() : "System");
        return "redirect:/admin?tab=activities";
    }

    @PostMapping("/mystery-box/themes/update")
    public String updateTheme(@RequestParam Long id,
            @RequestParam String name,
            @RequestParam java.math.BigDecimal price,
            @CurrentUser TokenService.TokenInfo info) {
        adminService.updateTheme(id, name, price, info != null ? info.getUsername() : "System");
        return "redirect:/admin?tab=mystery";
    }

    @PostMapping("/activities/update")
    public String updateActivity(@RequestParam Long id,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String expiryDate,
            @CurrentUser TokenService.TokenInfo info) {
        adminService.updateActivity(id, title, description, type, startDate, expiryDate,
                info != null ? info.getUsername() : "System");
        return "redirect:/admin?tab=activities";
    }

    @PostMapping("/activities/toggle/{id}")
    public String toggleActivity(@PathVariable Long id, @CurrentUser TokenService.TokenInfo info) {
        adminService.toggleActivity(id, info != null ? info.getUsername() : "System");
        return "redirect:/admin?tab=activities";
    }

    @PostMapping("/members/update")
    public String updateMember(@RequestParam Long id,
            @RequestParam String email,
            @RequestParam String nickname,
            @RequestParam boolean enabled,
            @RequestParam com.toy.store.model.MemberLevel level,
            @CurrentUser TokenService.TokenInfo info) {
        adminService.updateMember(id, email, nickname, enabled, level, info != null ? info.getUsername() : "System");
        return "redirect:/admin?tab=members";
    }

    @PostMapping("/categories/update")
    public String updateCategory(@RequestParam Long id, @RequestParam String name,
            @CurrentUser TokenService.TokenInfo info) {
        adminService.updateCategory(id, name, info != null ? info.getUsername() : "System");
        return "redirect:/admin?tab=categories";
    }

    @PostMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id, @CurrentUser TokenService.TokenInfo info) {
        adminService.deleteCategory(id, info != null ? info.getUsername() : "System");
        return "redirect:/admin?tab=categories";
    }

    @PostMapping("/subcategories/update")
    public String updateSubCategory(@RequestParam Long id, @RequestParam String name,
            @CurrentUser TokenService.TokenInfo info) {
        adminService.updateSubCategory(id, name, info != null ? info.getUsername() : "System");
        return "redirect:/admin?tab=categories";
    }

    @PostMapping("/subcategories/delete/{id}")
    public String deleteSubCategory(@PathVariable Long id, @CurrentUser TokenService.TokenInfo info) {
        adminService.deleteSubCategory(id, info != null ? info.getUsername() : "System");
        return "redirect:/admin?tab=categories";
    }

    @PostMapping("/member-levels")
    public String createMemberLevel(@ModelAttribute com.toy.store.model.MemberLevel memberLevel,
            @CurrentUser TokenService.TokenInfo info) {
        adminService.saveMemberLevel(memberLevel, info != null ? info.getUsername() : "System");
        return "redirect:/admin?tab=levels";
    }

    @PostMapping("/member-levels/update")
    public String updateMemberLevel(@ModelAttribute com.toy.store.model.MemberLevel memberLevel,
            @CurrentUser TokenService.TokenInfo info) {
        adminService.saveMemberLevel(memberLevel, info != null ? info.getUsername() : "System");
        return "redirect:/admin?tab=levels";
    }

    @PostMapping("/member-levels/delete/{id}")
    public String deleteMemberLevel(@PathVariable Long id, @CurrentUser TokenService.TokenInfo info) {
        adminService.deleteMemberLevel(id, info != null ? info.getUsername() : "System");
        return "redirect:/admin?tab=levels";
    }

    @PostMapping("/mystery-box/themes/delete/{id}")
    public String deleteMysteryBoxTheme(@PathVariable Long id, @CurrentUser TokenService.TokenInfo info) {
        adminService.deleteTheme(id, info != null ? info.getUsername() : "System");
        return "redirect:/admin?tab=mystery";
    }

    @PostMapping("/mystery-box/items/delete/{id}")
    public String deleteMysteryBoxItem(@PathVariable Long id, @CurrentUser TokenService.TokenInfo info) {
        adminService.deleteMysteryBoxItem(id, info != null ? info.getUsername() : "System");
        return "redirect:/admin?tab=mystery";
    }

    // --- Platform Management ---

    @PostMapping("/platform/carousel/create")
    @ResponseBody
    public org.springframework.http.ResponseEntity<Map<String, Object>> createCarouselSlide(
            @RequestParam String imageUrl, @RequestParam(required = false) String linkUrl,
            @CurrentUser TokenService.TokenInfo info) {

        Map<String, Object> response = new java.util.LinkedHashMap<>();
        try {
            com.toy.store.model.CarouselSlide saved = adminService.createCarouselSlide(imageUrl, linkUrl,
                    info != null ? info.getUsername() : "System");
            response.put("success", true);
            response.put("slide", saved);
            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return org.springframework.http.ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/platform/carousel/delete/{id}")
    @ResponseBody
    public org.springframework.http.ResponseEntity<Map<String, Object>> deleteCarouselSlide(
            @PathVariable Long id, @CurrentUser TokenService.TokenInfo info) {
        Map<String, Object> response = new java.util.LinkedHashMap<>();
        try {
            adminService.deleteCarouselSlide(id, info != null ? info.getUsername() : "System");
            response.put("success", true);
            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return org.springframework.http.ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/platform/featured/add")
    @ResponseBody
    public org.springframework.http.ResponseEntity<Map<String, Object>> addFeaturedItem(
            @RequestParam Long productId,
            @RequestParam com.toy.store.model.FeaturedItem.Type type,
            @RequestParam(defaultValue = "0") Integer sortOrder,
            @CurrentUser TokenService.TokenInfo info) {

        Map<String, Object> response = new LinkedHashMap<>();
        try {
            com.toy.store.model.FeaturedItem saved = adminService.addFeaturedItem(productId, type, sortOrder,
                    info != null ? info.getUsername() : "System");
            response.put("success", true);
            response.put("item", saved);
            response.put("productName", saved.getProduct().getName());
            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return org.springframework.http.ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/platform/featured/delete/{id}")
    @ResponseBody
    public org.springframework.http.ResponseEntity<Map<String, Object>> deleteFeaturedItem(
            @PathVariable Long id, @CurrentUser TokenService.TokenInfo info) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            adminService.deleteFeaturedItem(id, info != null ? info.getUsername() : "System");
            response.put("success", true);
            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return org.springframework.http.ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/platform/notification/send")
    @ResponseBody
    public org.springframework.http.ResponseEntity<Map<String, Object>> sendNotification(
            @ModelAttribute com.toy.store.model.Notification notification,
            @CurrentUser TokenService.TokenInfo info) {

        Map<String, Object> response = new LinkedHashMap<>();
        try {
            com.toy.store.model.Notification saved = adminService.sendNotification(notification,
                    info != null ? info.getUsername() : "System");
            response.put("success", true);
            response.put("notification", saved);
            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return org.springframework.http.ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/coupons")
    public String createCoupon(
            @RequestParam String name,
            @RequestParam String code,
            @RequestParam com.toy.store.model.Coupon.CouponType type,
            @RequestParam java.math.BigDecimal value,
            @RequestParam String description,
            @RequestParam(required = false) String validFrom,
            @RequestParam(required = false) String validUntil,
            @CurrentUser TokenService.TokenInfo info) {
        adminService.createCoupon(name, code, type, value, description, validFrom, validUntil,
                info != null ? info.getUsername() : "System");
        return "redirect:/admin?tab=coupons";
    }

    @PostMapping("/coupons/distribute/level")
    public String distributeToLevel(@RequestParam Long couponId, @RequestParam Long levelId,
            @CurrentUser TokenService.TokenInfo info) {
        adminService.distributeToLevel(couponId, levelId, info != null ? info.getUsername() : "System");
        return "redirect:/admin?tab=coupons";
    }

    @PostMapping("/coupons/distribute/member")
    public String distributeToMember(@RequestParam Long couponId, @RequestParam Long memberId,
            @CurrentUser TokenService.TokenInfo info) {
        adminService.distributeToMember(couponId, memberId, info != null ? info.getUsername() : "System");
        return "redirect:/admin?tab=coupons";
    }
}
