package com.toy.store.controller;

import com.toy.store.model.Product;
import com.toy.store.repository.MemberRepository;
import com.toy.store.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private com.toy.store.repository.MysteryBoxThemeRepository mysteryBoxThemeRepository;

    @Autowired
    private com.toy.store.repository.TransactionRepository transactionRepository;

    @Autowired
    private com.toy.store.repository.ActivityRepository activityRepository;

    @Autowired
    private com.toy.store.repository.MemberActionLogRepository memberActionLogRepository;

    @Autowired
    private com.toy.store.repository.CategoryRepository categoryRepository;

    @Autowired
    private com.toy.store.repository.SubCategoryRepository subCategoryRepository;

    @Autowired
    private com.toy.store.repository.MysteryBoxItemRepository mysteryBoxItemRepository;

    @Autowired
    private com.toy.store.repository.AdminActionLogRepository adminActionLogRepository;

    @GetMapping("/login")
    public String adminLogin() {
        return "admin_login";
    }

    @GetMapping
    public String adminDashboard(Model model) {
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

        model.addAttribute("newProduct", new Product());
        model.addAttribute("newTheme", new com.toy.store.model.MysteryBoxTheme());
        model.addAttribute("newActivity", new com.toy.store.model.Activity());
        model.addAttribute("newCategory", new com.toy.store.model.Category());

        return "admin";
    }

    private void logAction(String action, String details) {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        String adminName = (auth != null) ? auth.getName() : "Unknown";
        adminActionLogRepository.save(new com.toy.store.model.AdminActionLog(adminName, action, details));
    }

    // --- Product Management ---
    @PostMapping("/products")
    public String createProduct(@ModelAttribute Product product) {
        productService.saveProduct(product);
        logAction("CREATE_PRODUCT", "Created product: " + product.getName());
        return "redirect:/admin?tab=products";
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        logAction("DELETE_PRODUCT", "Deleted product ID: " + id);
        return "redirect:/admin?tab=products";
    }

    @PostMapping("/categories")
    public String createCategory(@ModelAttribute com.toy.store.model.Category category) {
        categoryRepository.save(category);
        logAction("CREATE_CATEGORY", "Created category: " + category.getName());
        return "redirect:/admin?tab=categories";
    }

    @PostMapping("/subcategories")
    public String createSubCategory(@RequestParam Long categoryId, @RequestParam String name) {
        com.toy.store.model.Category cat = categoryRepository.findById(categoryId).orElseThrow();
        com.toy.store.model.SubCategory sub = new com.toy.store.model.SubCategory();
        sub.setName(name);
        sub.setCategory(cat);
        subCategoryRepository.save(sub);
        logAction("CREATE_SUBCATEGORY", "Created subcategory: " + name + " in " + cat.getName());
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
    public String toggleMemberStatus(@PathVariable Long id) {
        com.toy.store.model.Member member = memberRepository.findById(id).orElseThrow();
        member.setEnabled(!member.isEnabled());
        memberRepository.save(member);
        logAction("TOGGLE_MEMBER", "Toggled status for member: " + member.getUsername() + " to " + member.isEnabled());
        return "redirect:/admin?tab=members";
    }

    @GetMapping("/api/members/{id}/history")
    @ResponseBody
    public java.util.Map<String, Object> getMemberHistory(@PathVariable Long id) {
        java.util.Map<String, Object> history = new java.util.HashMap<>();
        // Fetch transactions/orders/logs logic here
        // For now, returning simple data or empty
        return history;
    }

    // --- Mystery Box Management ---
    @PostMapping("/mystery-box/themes")
    public String createTheme(@ModelAttribute com.toy.store.model.MysteryBoxTheme theme) {
        mysteryBoxThemeRepository.save(theme);
        logAction("CREATE_THEME", "Created Theme: " + theme.getName());
        return "redirect:/admin?tab=mystery";
    }

    @PostMapping("/mystery-box/items")
    public String createMysteryBoxItem(@RequestParam Long themeId,
            @ModelAttribute com.toy.store.model.MysteryBoxItem item) {
        com.toy.store.model.MysteryBoxTheme theme = mysteryBoxThemeRepository.findById(themeId).orElseThrow();
        item.setTheme(theme);
        mysteryBoxItemRepository.save(item);
        logAction("CREATE_BOX_ITEM", "Added item " + item.getName() + " to theme " + theme.getName());
        return "redirect:/admin?tab=mystery";
    }

    // --- Activity Management ---
    @PostMapping("/activities")
    public String createActivity(@ModelAttribute com.toy.store.model.Activity activity) {
        activityRepository.save(activity);
        logAction("CREATE_ACTIVITY", "Created activity: " + activity.getTitle());
        return "redirect:/admin?tab=activities";
    }
}
