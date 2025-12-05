package com.toy.store.controller;

import com.toy.store.model.Cart;
import com.toy.store.security.services.UserDetailsImpl;
import com.toy.store.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public String getCart(@AuthenticationPrincipal UserDetailsImpl user, Model model) {
        Cart cart = cartService.getCartByMemberId(user.getId());
        model.addAttribute("cart", cart);
        model.addAttribute("totalPrice", cart.getItems().stream()
                .map(item -> item.getProduct().getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@AuthenticationPrincipal UserDetailsImpl user,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        cartService.addToCart(user.getId(), productId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String updateQuantity(@AuthenticationPrincipal UserDetailsImpl user,
            @RequestParam Long itemId,
            @RequestParam Integer quantity) {
        cartService.updateQuantity(user.getId(), itemId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeFromCart(@AuthenticationPrincipal UserDetailsImpl user,
            @RequestParam Long itemId) {
        // Quantity 0 triggers removal in service
        cartService.updateQuantity(user.getId(), itemId, 0);
        return "redirect:/cart";
    }

    // AJAX Endpoints
    @PostMapping("/api/remove")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> removeFromCartAjax(@AuthenticationPrincipal UserDetailsImpl user,
            @RequestParam Long itemId) {
        if (user == null) {
            return org.springframework.http.ResponseEntity.status(401)
                    .body(java.util.Map.of("success", false, "message", "請先登入"));
        }
        try {
            // Quantity 0 triggers removal
            cartService.updateQuantity(user.getId(), itemId, 0);
            Cart cart = cartService.getCartByMemberId(user.getId());
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
    public org.springframework.http.ResponseEntity<?> addToCartAjax(@AuthenticationPrincipal UserDetailsImpl user,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        if (user == null) {
            return org.springframework.http.ResponseEntity.status(401)
                    .body(java.util.Map.of("success", false, "message", "請先登入"));
        }
        try {
            cartService.addToCart(user.getId(), productId, quantity);
            Cart cart = cartService.getCartByMemberId(user.getId());
            int totalItems = cart.getItems().stream().mapToInt(item -> item.getQuantity()).sum();
            return org.springframework.http.ResponseEntity
                    .ok(java.util.Map.of("success", true, "totalItems", totalItems));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest()
                    .body(java.util.Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/api/items")
    public String getCartItemsFragment(@AuthenticationPrincipal UserDetailsImpl user, Model model) {
        if (user == null)
            return "";
        Cart cart = cartService.getCartByMemberId(user.getId());
        model.addAttribute("cart", cart);
        model.addAttribute("totalPrice", cart.getItems().stream()
                .map(item -> item.getProduct().getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        return "fragments/cart-items :: cart-items";
    }
}
