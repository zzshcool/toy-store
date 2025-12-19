package com.toy.store.controller;

import com.toy.store.annotation.CurrentUser;
import com.toy.store.model.Cart;
import com.toy.store.model.Member;
import com.toy.store.repository.MemberRepository;
import com.toy.store.service.CartService;
import com.toy.store.service.CouponService;
import com.toy.store.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CouponService couponService;

    private Member getMember(TokenService.TokenInfo info) {
        if (info == null)
            return null;
        return memberRepository.findByUsername(info.getUsername()).orElse(null);
    }

    @GetMapping
    public String getCart(@CurrentUser TokenService.TokenInfo info, Model model) {
        Member member = getMember(info);
        if (member == null)
            return "redirect:/login";

        Cart cart = cartService.getCartByMemberId(member.getId());
        model.addAttribute("cart", cart);
        model.addAttribute("totalPrice", cart.getItems().stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        model.addAttribute("availableCoupons", couponService.getMemberCoupons(member.getId()));
        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@CurrentUser TokenService.TokenInfo info,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        Member member = getMember(info);
        if (member == null)
            return "redirect:/login";

        cartService.addToCart(member.getId(), productId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String updateQuantity(@CurrentUser TokenService.TokenInfo info,
            @RequestParam Long itemId,
            @RequestParam Integer quantity) {
        Member member = getMember(info);
        if (member == null)
            return "redirect:/login";

        cartService.updateQuantity(member.getId(), itemId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeFromCart(@CurrentUser TokenService.TokenInfo info,
            @RequestParam Long itemId) {
        Member member = getMember(info);
        if (member == null)
            return "redirect:/login";

        // Quantity 0 triggers removal in service
        cartService.updateQuantity(member.getId(), itemId, 0);
        return "redirect:/cart";
    }

    // AJAX Endpoints
    @PostMapping("/api/remove")
    @ResponseBody
    public ResponseEntity<?> removeFromCartAjax(@CurrentUser TokenService.TokenInfo info,
            @RequestParam Long itemId) {
        Member member = getMember(info);
        if (member == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "請先登入"));
        }
        try {
            // Quantity 0 triggers removal
            cartService.updateQuantity(member.getId(), itemId, 0);
            Cart cart = cartService.getCartByMemberId(member.getId());
            int totalItems = cart.getItems().stream().mapToInt(item -> item.getQuantity()).sum();
            return ResponseEntity.ok(Map.of("success", true, "totalItems", totalItems));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/api/add")
    @ResponseBody
    public ResponseEntity<?> addToCartAjax(@CurrentUser TokenService.TokenInfo info,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        Member member = getMember(info);
        if (member == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "請先登入"));
        }
        try {
            cartService.addToCart(member.getId(), productId, quantity);
            Cart cart = cartService.getCartByMemberId(member.getId());
            int totalItems = cart.getItems().stream().mapToInt(item -> item.getQuantity()).sum();
            return ResponseEntity.ok(Map.of("success", true, "totalItems", totalItems));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/api/items")
    public String getCartItemsFragment(@CurrentUser TokenService.TokenInfo info, Model model) {
        Member member = getMember(info);
        if (member == null)
            return "";
        Cart cart = cartService.getCartByMemberId(member.getId());
        model.addAttribute("cart", cart);
        model.addAttribute("totalPrice", cart.getItems().stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return "fragments/cart-items :: cart-items";
    }
}
