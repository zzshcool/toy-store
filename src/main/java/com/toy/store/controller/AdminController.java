package com.toy.store.controller;

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

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberLevelRepository memberLevelRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private com.toy.store.repository.MysteryBoxThemeRepository mysteryBoxThemeRepository;

    @Autowired
    private com.toy.store.repository.TransactionRepository transactionRepository;

    @Autowired
    private com.toy.store.repository.ActivityRepository activityRepository;

    @Autowired
    private com.toy.store.repository.CategoryRepository categoryRepository;

    @Autowired
    private com.toy.store.repository.SubCategoryRepository subCategoryRepository;

    @Autowired
    private com.toy.store.repository.MysteryBoxItemRepository mysteryBoxItemRepository;

    @Autowired
    private com.toy.store.repository.AdminActionLogRepository adminActionLogRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private com.toy.store.repository.AdminUserRepository adminUserRepository;

    @GetMapping("/login")
    public String adminLogin() {
        return "admin_login";
    }

    @PostMapping("/login-submit")
    public String loginSubmit(@RequestParam String username, @RequestParam String password,
                              HttpServletResponse response) {

        java.util.Optional<com.toy.store.model.AdminUser> adminOpt = adminUserRepository.findByUsername(username);

        if (adminOpt.isPresent()) {
            if (passwordEncoder.matches(password, adminOpt.get().getPassword())) {
                // Success
                String token = tokenService.createToken(username, TokenService.ROLE_ADMIN);
                Cookie cookie = new Cookie("ADMIN_TOKEN", token);
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                cookie.setMaxAge(28800);
                response.addCookie(cookie);
                return "redirect:/admin";
            }
        }
        return "redirect:/admin/login?error";
    }

    @RequestMapping(value = "/logout", method = {RequestMethod.GET, RequestMethod.POST})
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
    public String adminDashboard(Model model, HttpServletRequest request) {
        try {
            TokenService.TokenInfo info = (TokenService.TokenInfo) request.getAttribute("currentUser");
            if (info != null) {
                model.addAttribute("adminName", info.getUsername());
            }
            model.addAttribute("members", memberRepository.findAll());
            model.addAttribute("products",
                    productService.findAll(org.springframework.data.domain.Pageable.unpaged()).getContent());
            model.addAttribute("mysteryBoxThemes", mysteryBoxThemeRepository.findAll());
            model.addAttribute("transactions", transactionRepository.findAll(org.springframework.data.domain.Sort
                    .by(org.springframework.data.domain.Sort.Direction.DESC, "timestamp")));
            model.addAttribute("activities", activityRepository.findAll());
            model.addAttribute("logs", adminActionLogRepository.findAll(org.springframework.data.domain.Sort
                    .by(org.springframework.data.domain.Sort.Direction.DESC, "timestamp")));
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("memberLevels", memberLevelRepository.findAllByOrderBySortOrderAsc());

            model.addAttribute("newProduct", new Product());
            model.addAttribute("newTheme", new com.toy.store.model.MysteryBoxTheme());
            model.addAttribute("newActivity", new com.toy.store.model.Activity());
            model.addAttribute("newCategory", new com.toy.store.model.Category());
        } catch (Exception e) {
            e.printStackTrace(); // Print to server console for good measure
            model.addAttribute("errorMessage", "系統錯誤: " + e.getMessage() + " (" + e.getClass().getSimpleName() + ")");
            // Initialize empty lists to prevent template crashing on null
            model.addAttribute("members", java.util.Collections.emptyList());
            model.addAttribute("products", java.util.Collections.emptyList());
            model.addAttribute("mysteryBoxThemes", java.util.Collections.emptyList());
            model.addAttribute("transactions", java.util.Collections.emptyList());
            model.addAttribute("activities", java.util.Collections.emptyList());
            model.addAttribute("logs", java.util.Collections.emptyList());
            model.addAttribute("categories", java.util.Collections.emptyList());
            model.addAttribute("newProduct", new Product());
            model.addAttribute("newTheme", new com.toy.store.model.MysteryBoxTheme());
            model.addAttribute("newActivity", new com.toy.store.model.Activity());
            model.addAttribute("newCategory", new com.toy.store.model.Category());
        }

        return "admin";
    }

    private void logAction(String action, String details, HttpServletRequest request) {
        TokenService.TokenInfo info = (TokenService.TokenInfo) request.getAttribute("currentUser");
        String adminName = (info != null) ? info.getUsername() : "Unknown";

        StringBuilder params = new StringBuilder();
        java.util.Map<String, String[]> map = request.getParameterMap();
        for (String key : map.keySet()) {
            params.append(key).append("=").append(java.util.Arrays.toString(map.get(key))).append("; ");
        }

        adminActionLogRepository
                .save(new com.toy.store.model.AdminActionLog(adminName, action, details, params.toString()));
    }

    // --- Product Management ---
    @PostMapping("/products")
    public String createProduct(@ModelAttribute Product product, HttpServletRequest request) {
        productService.saveProduct(product);
        logAction("CREATE_PRODUCT", "Created product: " + product.getName(), request);
        return "redirect:/admin?tab=products";
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, HttpServletRequest request) {
        productService.deleteProduct(id);
        logAction("DELETE_PRODUCT", "Deleted product ID: " + id, request);
        return "redirect:/admin?tab=products";
    }

    @PostMapping("/categories")
    public String createCategory(@ModelAttribute com.toy.store.model.Category category, HttpServletRequest request) {
        categoryRepository.save(category);
        logAction("CREATE_CATEGORY", "Created category: " + category.getName(), request);
        return "redirect:/admin?tab=categories";
    }

    @PostMapping("/subcategories")
    public String createSubCategory(@RequestParam Long categoryId, @RequestParam String name,
                                    HttpServletRequest request) {
        com.toy.store.model.Category cat = categoryRepository.findById(categoryId).orElseThrow();
        com.toy.store.model.SubCategory sub = new com.toy.store.model.SubCategory();
        sub.setName(name);
        sub.setCategory(cat);
        subCategoryRepository.save(sub);
        logAction("CREATE_SUBCATEGORY", "Created subcategory: " + name + " in " + cat.getName(), request);
        return "redirect:/admin?tab=categories";
    }

    @GetMapping("/api/subcategories/{categoryId}")
    @ResponseBody
    public java.util.List<com.toy.store.model.SubCategory> getSubCategories(@PathVariable Long categoryId) {
        return categoryRepository.findById(categoryId).map(com.toy.store.model.Category::getSubCategories)
                .orElse(java.util.Collections.emptyList());
    }

    // --- Member Management ---
    @PostMapping("/members/{id}/toggle-status")
    public String toggleMemberStatus(@PathVariable Long id, HttpServletRequest request) {
        com.toy.store.model.Member member = memberRepository.findById(id).orElseThrow();
        member.setEnabled(!member.isEnabled());
        memberRepository.save(member);
        logAction("TOGGLE_MEMBER", "Toggled status for member: " + member.getUsername() + " to " + member.isEnabled(),
                request);
        return "redirect:/admin?tab=members";
    }

    @GetMapping("/api/members/{id}/history")
    @ResponseBody
    public java.util.Map<String, Object> getMemberHistory(@PathVariable Long id) {
        java.util.Map<String, Object> history = new java.util.HashMap<>();

        com.toy.store.model.Member member = memberRepository.findById(id).orElse(null);
        if (member == null) {
            history.put("error", "Member not found");
            return history;
        }

        // Transactions (Top-up, Purchase)
        java.util.List<com.toy.store.model.Transaction> transactions = transactionRepository
                .findByMemberIdOrderByTimestampDesc(id);

        // Mock Logs (Since we don't have MemberActionLog entity yet, we display what we
        // have)
        // In a real scenario, we would query MemberActionLogRepository

        history.put("transactions", transactions);
        history.put("member", member);
        // Add other history items if available

        return history;
    }

    // --- Mystery Box Management ---
    @PostMapping("/mystery-box/themes")
    public String createTheme(@ModelAttribute com.toy.store.model.MysteryBoxTheme theme, HttpServletRequest request) {
        mysteryBoxThemeRepository.save(theme);
        logAction("CREATE_THEME", "Created Theme: " + theme.getName(), request);
        return "redirect:/admin?tab=mystery";
    }

    @PostMapping("/mystery-box/items")
    public String createMysteryBoxItem(@RequestParam Long themeId,
                                       @ModelAttribute com.toy.store.model.MysteryBoxItem item, HttpServletRequest request) {
        com.toy.store.model.MysteryBoxTheme theme = mysteryBoxThemeRepository.findById(themeId).orElseThrow();
        item.setTheme(theme);
        mysteryBoxItemRepository.save(item);
        logAction("CREATE_BOX_ITEM", "Added item " + item.getName() + " to theme " + theme.getName(), request);
        return "redirect:/admin?tab=mystery";
    }

    // --- Activity Management ---
    @PostMapping("/activities")
    public String createActivity(@ModelAttribute com.toy.store.model.Activity activity, HttpServletRequest request) {
        activityRepository.save(activity);
        logAction("CREATE_ACTIVITY", "Created activity: " + activity.getTitle(), request);
        return "redirect:/admin?tab=activities";
    }

    @PostMapping("/admin/mystery-box/themes/update")
    public String updateTheme(@RequestParam Long id,
                              @RequestParam String name,
                              @RequestParam java.math.BigDecimal price,
                              HttpServletRequest request) {
        mysteryBoxThemeRepository.findById(id).ifPresent(theme -> {
            theme.setName(name);
            theme.setPrice(price);
            mysteryBoxThemeRepository.save(theme);
            logAction("UPDATE_THEME", "Updated theme: " + name, request);
        });
        return "redirect:/admin?tab=mystery";
    }

    @PostMapping("/admin/activities/add")
    public String addActivity(@ModelAttribute("newActivity") com.toy.store.model.Activity activity,
                              HttpServletRequest request) {
        activityRepository.save(activity);
        logAction("CREATE_ACTIVITY", "Created activity: " + activity.getTitle(), request);
        return "redirect:/admin";
    }

    @PostMapping("/admin/activities/delete/{id}")
    public String deleteActivity(@PathVariable Long id, HttpServletRequest request) {
        activityRepository.findById(id).ifPresent(activity -> {
            activityRepository.delete(activity);
            logAction("DELETE_ACTIVITY", "Deleted activity: " + activity.getTitle(), request);
        });
        return "redirect:/admin";
    }

    @PostMapping("/admin/activities/toggle/{id}")
    public String toggleActivity(@PathVariable Long id, HttpServletRequest request) {
        activityRepository.findById(id).ifPresent(activity -> {
            activity.setActive(!activity.isActive());
            activityRepository.save(activity);
            logAction("TOGGLE_ACTIVITY", "Toggled activity: " + activity.getTitle() + " to " + activity.isActive(),
                    request);
        });
        return "redirect:/admin";
    }

    @PostMapping("/admin/activities/update")
    public String updateActivity(@RequestParam Long id,
                                 @RequestParam String title,
                                 @RequestParam String description,
                                 @RequestParam String type,
                                 @RequestParam(required = false) String startDate,
                                 @RequestParam(required = false) String expiryDate,
                                 HttpServletRequest request) {
        activityRepository.findById(id).ifPresent(activity -> {
            activity.setTitle(title);
            activity.setDescription(description);
            activity.setType(type);
            if (startDate != null && !startDate.isEmpty()) {
                activity.setStartDate(java.time.LocalDateTime.parse(startDate));
            }
            if (expiryDate != null && !expiryDate.isEmpty()) {
                activity.setExpiryDate(java.time.LocalDateTime.parse(expiryDate));
            }
            activityRepository.save(activity);
            logAction("UPDATE_ACTIVITY", "Updated activity: " + title, request);
        });
        return "redirect:/admin";
    }

    @PostMapping("/admin/members/update")
    public String updateMember(@RequestParam Long id,
                               @RequestParam String email,
                               @RequestParam String nickname,
                               @RequestParam boolean enabled,
                               @RequestParam com.toy.store.model.MemberLevel level,
                               HttpServletRequest request) {
        memberRepository.findById(id).ifPresent(member -> {
            member.setEmail(email);
            member.setNickname(nickname);
            member.setEnabled(enabled);
            member.setLevel(level);
            memberRepository.save(member);
            logAction("UPDATE_MEMBER", "Updated member: " + member.getUsername() + " to level " + level, request);
        });
        return "redirect:/admin?tab=members";
    }

    @PostMapping("/admin/categories/update")
    public String updateCategory(@RequestParam Long id, @RequestParam String name, HttpServletRequest request) {
        categoryRepository.findById(id).ifPresent(cat -> {
            cat.setName(name);
            categoryRepository.save(cat);
            logAction("UPDATE_CATEGORY", "Updated category: " + name, request);
        });
        return "redirect:/admin?tab=categories";
    }

    @PostMapping("/admin/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id, HttpServletRequest request) {
        categoryRepository.findById(id).ifPresent(cat -> {
            categoryRepository.delete(cat);
            logAction("DELETE_CATEGORY", "Deleted category: " + cat.getName(), request);
        });
        return "redirect:/admin?tab=categories";
    }

    @PostMapping("/admin/subcategories/update")
    public String updateSubCategory(@RequestParam Long id, @RequestParam String name, HttpServletRequest request) {
        subCategoryRepository.findById(id).ifPresent(sub -> {
            sub.setName(name);
            subCategoryRepository.save(sub);
            logAction("UPDATE_SUBCATEGORY", "Updated subcategory: " + name, request);
        });
        return "redirect:/admin?tab=categories";
    }

    @PostMapping("/admin/subcategories/delete/{id}")
    public String deleteSubCategory(@PathVariable Long id, HttpServletRequest request) {
        subCategoryRepository.findById(id).ifPresent(sub -> {
            subCategoryRepository.delete(sub);
            logAction("DELETE_SUBCATEGORY", "Deleted subcategory: " + sub.getName(), request);
        });
        return "redirect:/admin?tab=categories";
    }

    @PostMapping("/member-levels")
    public String createMemberLevel(@ModelAttribute com.toy.store.model.MemberLevel memberLevel,
                                    HttpServletRequest request) {
        memberLevelRepository.save(memberLevel);
        logAction("CREATE_MEMBER_LEVEL", "Created level: " + memberLevel.getName(), request);
        return "redirect:/admin?tab=levels";
    }

    @PostMapping("/member-levels/update")
    public String updateMemberLevel(@ModelAttribute com.toy.store.model.MemberLevel memberLevel,
                                    HttpServletRequest request) {
        memberLevelRepository.save(memberLevel);
        logAction("UPDATE_MEMBER_LEVEL", "Updated level: " + memberLevel.getName(), request);
        return "redirect:/admin?tab=levels";
    }

    @PostMapping("/member-levels/delete/{id}")
    public String deleteMemberLevel(@PathVariable Long id, HttpServletRequest request) {
        memberLevelRepository.deleteById(id);
        logAction("DELETE_MEMBER_LEVEL", "Deleted level ID: " + id, request);
        return "redirect:/admin?tab=levels";
    }

    @PostMapping("/mystery-box/themes/update")
    public String updateMysteryBoxTheme(@RequestParam Long id, @RequestParam String name, @RequestParam Double price,
                                        HttpServletRequest request) {
        mysteryBoxThemeRepository.findById(id).ifPresent(theme -> {
            theme.setName(name);
            theme.setPrice(java.math.BigDecimal.valueOf(price));
            mysteryBoxThemeRepository.save(theme);
            logAction("UPDATE_THEME", "Updated theme ID: " + id, request);
        });
        return "redirect:/admin?tab=mystery";
    }

    @PostMapping("/mystery-box/themes/delete/{id}")
    public String deleteMysteryBoxTheme(@PathVariable Long id, HttpServletRequest request) {
        mysteryBoxThemeRepository.deleteById(id);
        logAction("DELETE_THEME", "Deleted theme ID: " + id, request);
        return "redirect:/admin?tab=mystery";
    }

    @PostMapping("/mystery-box/items/update")
    public String updateMysteryBoxItem(@RequestParam Long id, @RequestParam String name,
                                       @RequestParam Double estimatedValue, @RequestParam Integer weight, HttpServletRequest request) {
        mysteryBoxItemRepository.findById(id).ifPresent(item -> {
            item.setName(name);
            item.setEstimatedValue(java.math.BigDecimal.valueOf(estimatedValue));
            item.setWeight(weight);
            mysteryBoxItemRepository.save(item);
            logAction("UPDATE_ITEM", "Updated item ID: " + id, request);
        });
        return "redirect:/admin?tab=mystery";
    }

    @PostMapping("/mystery-box/items/delete/{id}")
    public String deleteMysteryBoxItem(@PathVariable Long id, HttpServletRequest request) {
        mysteryBoxItemRepository.deleteById(id);
        logAction("DELETE_ITEM", "Deleted item ID: " + id, request);
        return "redirect:/admin?tab=mystery";
    }
}
