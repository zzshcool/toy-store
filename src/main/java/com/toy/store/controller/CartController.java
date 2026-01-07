package com.toy.store.controller;

import com.toy.store.annotation.CurrentUser;
import com.toy.store.model.Cart;
import com.toy.store.model.Member;
import com.toy.store.mapper.MemberMapper;
import com.toy.store.service.CartService;
import com.toy.store.service.CouponService;
import com.toy.store.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final MemberMapper memberMapper;
    private final CouponService couponService;

    private Optional<Member> getMember(TokenService.TokenInfo info) {
        if (info == null)
            return Optional.empty();
        return memberMapper.findByUsername(info.getUsername());
    }

    @GetMapping
    public String getCart(@CurrentUser TokenService.TokenInfo info, Model model) {
        return getMember(info).map(member -> {
            Cart cart = cartService.getCartByMemberId(member.getId());
            model.addAttribute("cart", cart);
            model.addAttribute("totalPrice", cart.getItems().stream()
                    .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            model.addAttribute("availableCoupons", couponService.getMemberCoupons(member.getId()));
            return "cart";
        }).orElse("redirect:/login");
    }

    @PostMapping("/add")
    public String addToCart(@CurrentUser TokenService.TokenInfo info,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        return getMember(info).map(member -> {
            cartService.addToCart(member.getId(), productId, quantity);
            return "redirect:/cart";
        }).orElse("redirect:/login");
    }

    @PostMapping("/update")
    public String updateQuantity(@CurrentUser TokenService.TokenInfo info,
            @RequestParam Long itemId,
            @RequestParam Integer quantity) {
        return getMember(info).map(member -> {
            cartService.updateQuantity(member.getId(), itemId, quantity);
            return "redirect:/cart";
        }).orElse("redirect:/login");
    }

    @PostMapping("/remove")
    public String removeFromCart(@CurrentUser TokenService.TokenInfo info,
            @RequestParam Long itemId) {
        return getMember(info).map(member -> {
            cartService.updateQuantity(member.getId(), itemId, 0);
            return "redirect:/cart";
        }).orElse("redirect:/login");
    }

    // AJAX Endpoints
    @PostMapping("/api/remove")
    @ResponseBody
    public ResponseEntity<?> removeFromCartAjax(@CurrentUser TokenService.TokenInfo info,
            @RequestParam Long itemId) {
        return getMember(info).map(member -> {
            try {
                cartService.updateQuantity(member.getId(), itemId, 0);
                Cart cart = cartService.getCartByMemberId(member.getId());
                int totalItems = cart.getItems().stream().mapToInt(item -> item.getQuantity()).sum();
                return ResponseEntity.ok(Map.of("success", true, "totalItems", totalItems));
            } catch (Exception e) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", e.getMessage()));
            }
        }).orElseGet(() -> ResponseEntity.status(401)
                .body(Map.of("success", false, "message", "請先登入")));
    }

    @PostMapping("/api/add")
    @ResponseBody
    public ResponseEntity<?> addToCartAjax(@CurrentUser TokenService.TokenInfo info,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        return getMember(info).map(member -> {
            try {
                cartService.addToCart(member.getId(), productId, quantity);
                Cart cart = cartService.getCartByMemberId(member.getId());
                int totalItems = cart.getItems().stream().mapToInt(item -> item.getQuantity()).sum();
                return ResponseEntity.ok(Map.of("success", true, "totalItems", totalItems));
            } catch (Exception e) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", e.getMessage()));
            }
        }).orElseGet(() -> ResponseEntity.status(401)
                .body(Map.of("success", false, "message", "請先登入")));
    }

    @GetMapping("/api/items")
    public String getCartItemsFragment(@CurrentUser TokenService.TokenInfo info, Model model) {
        return getMember(info).map(member -> {
            Cart cart = cartService.getCartByMemberId(member.getId());
            model.addAttribute("cart", cart);
            model.addAttribute("totalPrice", cart.getItems().stream()
                    .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            return "fragments/cart-items :: cart-items";
        }).orElse("");
    }
}
