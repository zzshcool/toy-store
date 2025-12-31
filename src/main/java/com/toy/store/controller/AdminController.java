package com.toy.store.controller;

import com.toy.store.annotation.CurrentUser;
import com.toy.store.model.*;
import com.toy.store.repository.AdminActionLogRepository;
import com.toy.store.repository.AdminUserRepository;
import com.toy.store.repository.CategoryRepository;
import com.toy.store.service.AdminService;
import com.toy.store.service.TokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final AdminUserRepository adminUserRepository;
    private final AdminActionLogRepository adminActionLogRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;

    @GetMapping("/login")
    public String adminLogin() {
        return "admin_login";
    }

    @PostMapping("/login-submit")
    public String loginSubmit(@RequestParam String username, @RequestParam String password,
            HttpServletResponse response, HttpServletRequest request) {

        Optional<AdminUser> adminOpt = adminUserRepository.findByUsername(username);

        if (adminOpt.isPresent()) {
            AdminUser admin = adminOpt.get();
            if (passwordEncoder.matches(password, admin.getPassword())) {
                String token = tokenService.createToken(username, TokenService.ROLE_ADMIN, admin.getPermissions());
                Cookie cookie = new Cookie("ADMIN_TOKEN", token);
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                cookie.setMaxAge(28800);
                response.addCookie(cookie);
                return "redirect:/admin";
            }
        }

        adminActionLogRepository.save(new AdminActionLog(
                username, "LOGIN_FAILED", "Invalid credentials", "IP: " + request.getRemoteAddr()));

        return "redirect:/admin/login?error";
    }

    @RequestMapping(value = "/logout", method = { RequestMethod.GET, RequestMethod.POST })
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("ADMIN_TOKEN".equals(cookie.getName())) {
                    tokenService.invalidateAdminToken(cookie.getValue());
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

        model.addAttribute("newProduct", new Product());
        model.addAttribute("newTheme", new GachaTheme());
        model.addAttribute("newActivity", new Activity());
        model.addAttribute("newCategory", new Category());

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
    public String createCategory(@ModelAttribute Category category, @CurrentUser TokenService.TokenInfo info) {
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
    public List<SubCategory> getSubCategories(@PathVariable Long categoryId) {
        return categoryRepository.findById(categoryId)
                .map(Category::getSubCategories)
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

    // --- Gacha Management ---
    @PostMapping("/gacha/themes")
    public String createTheme(@ModelAttribute GachaTheme theme, @CurrentUser TokenService.TokenInfo info) {
        adminService.createTheme(theme, info != null ? info.getUsername() : "System");
        return "redirect:/admin?tab=mystery";
    }

    @PostMapping("/gacha/items")
    public String createGachaItem(@RequestParam Long themeId, @ModelAttribute GachaItem item,
            @CurrentUser TokenService.TokenInfo info) {
        adminService.createGachaItem(themeId, item, info != null ? info.getUsername() : "System");
        return "redirect:/admin?tab=mystery";
    }

    @PostMapping("/gacha/items/update")
    public String updateGachaItem(@RequestParam Long id, @RequestParam String name,
            @RequestParam BigDecimal estimatedValue, @RequestParam Integer weight,
            @CurrentUser TokenService.TokenInfo info) {
        adminService.updateGachaItem(id, name, estimatedValue, weight, info != null ? info.getUsername() : "System");
        return "redirect:/admin?tab=mystery";
    }

    // --- Activity Management ---
    @PostMapping("/activities/delete/{id}")
    public String deleteActivity(@PathVariable Long id, @CurrentUser TokenService.TokenInfo info) {
        adminService.deleteActivity(id, info != null ? info.getUsername() : "System");
        return "redirect:/admin?tab=activities";
    }

    @PostMapping("/gacha/themes/update")
    public String updateTheme(@RequestParam Long id, @RequestParam String name, @RequestParam BigDecimal price,
            @CurrentUser TokenService.TokenInfo info) {
        adminService.updateTheme(id, name, price, info != null ? info.getUsername() : "System");
        return "redirect:/admin?tab=mystery";
    }

    @PostMapping("/activities/update")
    public String updateActivity(@RequestParam Long id, @RequestParam String title, @RequestParam String description,
            @RequestParam String type, @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String expiryDate, @CurrentUser TokenService.TokenInfo info) {
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
    public String updateMember(@RequestParam Long id, @RequestParam String email, @RequestParam String nickname,
            @RequestParam boolean enabled, @RequestParam MemberLevel level, @CurrentUser TokenService.TokenInfo info) {
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
    public String createMemberLevel(@ModelAttribute MemberLevel memberLevel, @CurrentUser TokenService.TokenInfo info) {
        adminService.saveMemberLevel(memberLevel, info != null ? info.getUsername() : "System");
        return "redirect:/admin?tab=levels";
    }

    @PostMapping("/member-levels/update")
    public String updateMemberLevel(@ModelAttribute MemberLevel memberLevel, @CurrentUser TokenService.TokenInfo info) {
        adminService.saveMemberLevel(memberLevel, info != null ? info.getUsername() : "System");
        return "redirect:/admin?tab=levels";
    }

    @PostMapping("/member-levels/delete/{id}")
    public String deleteMemberLevel(@PathVariable Long id, @CurrentUser TokenService.TokenInfo info) {
        adminService.deleteMemberLevel(id, info != null ? info.getUsername() : "System");
        return "redirect:/admin?tab=levels";
    }

    @PostMapping("/gacha/themes/delete/{id}")
    public String deleteGachaTheme(@PathVariable Long id, @CurrentUser TokenService.TokenInfo info) {
        adminService.deleteTheme(id, info != null ? info.getUsername() : "System");
        return "redirect:/admin?tab=mystery";
    }

    @PostMapping("/gacha/items/delete/{id}")
    public String deleteGachaItem(@PathVariable Long id, @CurrentUser TokenService.TokenInfo info) {
        adminService.deleteGachaItem(id, info != null ? info.getUsername() : "System");
        return "redirect:/admin?tab=mystery";
    }

    // --- Platform Management ---

    @PostMapping("/platform/carousel/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createCarouselSlide(
            @RequestParam String imageUrl, @RequestParam(required = false) String linkUrl,
            @CurrentUser TokenService.TokenInfo info) {

        Map<String, Object> response = new LinkedHashMap<>();
        try {
            CarouselSlide saved = adminService.createCarouselSlide(imageUrl, linkUrl,
                    info != null ? info.getUsername() : "System");
            response.put("success", true);
            response.put("slide", saved);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/platform/carousel/delete/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteCarouselSlide(
            @PathVariable Long id, @CurrentUser TokenService.TokenInfo info) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            adminService.deleteCarouselSlide(id, info != null ? info.getUsername() : "System");
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/platform/featured/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addFeaturedItem(
            @RequestParam Long productId, @RequestParam FeaturedItem.Type type,
            @RequestParam(defaultValue = "0") Integer sortOrder, @CurrentUser TokenService.TokenInfo info) {

        Map<String, Object> response = new LinkedHashMap<>();
        try {
            FeaturedItem saved = adminService.addFeaturedItem(productId, type, sortOrder,
                    info != null ? info.getUsername() : "System");
            response.put("success", true);
            response.put("item", saved);
            response.put("productName", saved.getProduct().getName());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/platform/featured/delete/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteFeaturedItem(
            @PathVariable Long id, @CurrentUser TokenService.TokenInfo info) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            adminService.deleteFeaturedItem(id, info != null ? info.getUsername() : "System");
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/platform/notification/send")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendNotification(
            @ModelAttribute Notification notification, @CurrentUser TokenService.TokenInfo info) {

        Map<String, Object> response = new LinkedHashMap<>();
        try {
            Notification saved = adminService.sendNotification(notification,
                    info != null ? info.getUsername() : "System");
            response.put("success", true);
            response.put("notification", saved);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/coupons")
    public String createCoupon(
            @RequestParam String name, @RequestParam String code, @RequestParam Coupon.CouponType type,
            @RequestParam BigDecimal value, @RequestParam String description,
            @RequestParam(required = false) String validFrom, @RequestParam(required = false) String validUntil,
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

    @GetMapping("/api/rtp-stats")
    @ResponseBody
    public List<Map<String, Object>> getRtpStats() {
        return adminService.getRtpStats();
    }
}
