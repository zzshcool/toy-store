package com.toy.store.controller;

import com.toy.store.model.Member;
import com.toy.store.model.MemberActionLog;
import com.toy.store.model.Product;
import com.toy.store.repository.MemberActionLogRepository;
import com.toy.store.repository.MemberRepository;
import com.toy.store.repository.ProductRepository;
import com.toy.store.service.ProductService;
import com.toy.store.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductRepository productRepository;
    private final MemberActionLogRepository memberActionLogRepository;
    private final MemberRepository memberRepository;

    @GetMapping
    public String getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subCategory,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Model model, HttpServletRequest request) {

        // Log Action (if user is logged in)
        TokenService.TokenInfo info = (TokenService.TokenInfo) request.getAttribute("currentUser");
        if (info != null && TokenService.ROLE_USER.equals(info.getRole())) {
            memberRepository.findByUsername(info.getUsername()).ifPresent(member -> {
                String details = "Page: " + page;
                if (keyword != null)
                    details += ", Keyword: " + keyword;
                if (category != null)
                    details += ", Category: " + category;
                memberActionLogRepository.save(new MemberActionLog(
                        member.getId(), member.getUsername(), "VIEW_PRODUCTS", details, true));
            });
        }

        Pageable pageable = PageRequest.of(page, size,
                direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending());

        Page<Product> productPage;

        if (keyword != null && !keyword.isEmpty()) {
            productPage = productService.searchProducts(keyword, pageable);
            model.addAttribute("keyword", keyword);
        } else if (category != null && !category.isEmpty()) {
            if (subCategory != null && !subCategory.isEmpty()) {
                productPage = productRepository.findByCategoryAndSubCategory(category, subCategory, pageable);
                model.addAttribute("currentSubCategory", subCategory);
            } else {
                productPage = productRepository.findByCategory(category, pageable);
            }
            model.addAttribute("currentCategory", category);
        } else {
            productPage = productService.findAll(pageable);
        }

        model.addAttribute("products", productPage);

        // Hierarchy Data for Sidebar
        Map<String, List<String>> categories = new LinkedHashMap<>();
        categories.put("鋼彈系列", Arrays.asList("鋼彈W", "鋼彈G武鬥", "鋼彈Seed", "無敵鐵金剛", "鋼彈(夏亞逆襲)"));
        categories.put("任天堂系列", Arrays.asList("超級瑪莉", "神奇寶貝"));
        categories.put("Capcom系列", Arrays.asList("元祖洛克人", "洛克人X", "洛克人EX"));
        model.addAttribute("categories", categories);

        return "products";
    }
}
