package com.toy.store.controller;

import com.toy.store.model.Cart;
import com.toy.store.model.Member;
import com.toy.store.repository.MemberRepository;
import com.toy.store.service.CartService;
import com.toy.store.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private MemberRepository memberRepository;

    private Member getMemberFromRequest(HttpServletRequest request) {
        TokenService.TokenInfo info = (TokenService.TokenInfo) request.getAttribute("currentUser");
        if (info == null)
            return null;
        return memberRepository.findByUsername(info.getUsername()).orElse(null);
    }

    @Autowired
    private com.toy.store.service.CouponService couponService;

    @GetMapping
    public String getCart(HttpServletRequest request, Model model) {
        Member member = getMemberFromRequest(request);
        if (member == null)
            return "redirect:/login";

        Cart cart = cartService.getCartByMemberId(member.getId());
        model.addAttribute("cart", cart);
        model.addAttribute("totalPrice", cart.getItems().stream()
                .map(item -> item.getProduct().getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        model.addAttribute("availableCoupons", couponService.getMemberCoupons(member.getId()));
        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(HttpServletRequest request,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        Member member = getMemberFromRequest(request);
        if (member == null)
            return "redirect:/login";

        cartService.addToCart(member.getId(), productId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String updateQuantity(HttpServletRequest request,
            @RequestParam Long itemId,
            @RequestParam Integer quantity) {
        Member member = getMemberFromRequest(request);
        if (member == null)
            return "redirect:/login";

        cartService.updateQuantity(member.getId(), itemId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeFromCart(HttpServletRequest request,
            @RequestParam Long itemId) {
        Member member = getMemberFromRequest(request);
        if (member == null)
            return "redirect:/login";

        // Quantity 0 triggers removal in service
        cartService.updateQuantity(member.getId(), itemId, 0);
        return "redirect:/cart";
    }

    // AJAX Endpoints
    @PostMapping("/api/remove")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> removeFromCartAjax(HttpServletRequest request,
            @RequestParam Long itemId) {
        Member member = getMemberFromRequest(request);
        if (member == null) {
            return org.springframework.http.ResponseEntity.status(401)
                    .body(java.util.Map.of("success", false, "message", "請先登入"));
        }
        try {
            // Quantity 0 triggers removal
            cartService.updateQuantity(member.getId(), itemId, 0);
            Cart cart = cartService.getCartByMemberId(member.getId());
            int totalItems = cart.getItems().stream().mapToInt(item -> item.getQuantity()).sum();
            return org.springframework.http.ResponseEntity
                    .ok(java.util.Map.of("success", true, "totalItems", totalItems));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest()
                    .body(java.util.Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/api/add")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> addToCartAjax(HttpServletRequest request,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        Member member = getMemberFromRequest(request);
        if (member == null) {
            return org.springframework.http.ResponseEntity.status(401)
                    .body(java.util.Map.of("success", false, "message", "請先登入"));
        }
        try {
            cartService.addToCart(member.getId(), productId, quantity);
            Cart cart = cartService.getCartByMemberId(member.getId());
            int totalItems = cart.getItems().stream().mapToInt(item -> item.getQuantity()).sum();
            return org.springframework.http.ResponseEntity
                    .ok(java.util.Map.of("success", true, "totalItems", totalItems));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest()
                    .body(java.util.Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/api/items")
    public String getCartItemsFragment(HttpServletRequest request, Model model) {
        Member member = getMemberFromRequest(request);
        if (member == null)
            return "";
        Cart cart = cartService.getCartByMemberId(member.getId());
        model.addAttribute("cart", cart);
        model.addAttribute("totalPrice", cart.getItems().stream()
                .map(item -> item.getProduct().getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        return "fragments/cart-items :: cart-items";
    }
}
