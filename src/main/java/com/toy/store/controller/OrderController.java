package com.toy.store.controller;

import com.toy.store.annotation.CurrentUser;
import com.toy.store.model.Member;
import com.toy.store.model.Order;
import com.toy.store.mapper.MemberMapper;
import com.toy.store.service.OrderService;
import com.toy.store.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final MemberMapper memberMapper;

    private Optional<Member> getMember(TokenService.TokenInfo info) {
        if (info == null)
            return Optional.empty();
        return memberMapper.findByUsername(info.getUsername());
    }

    @PostMapping("/checkout")
    public String checkout(@CurrentUser TokenService.TokenInfo info,
            @RequestParam(required = false) Long couponId,
            RedirectAttributes redirectAttributes) {
        return getMember(info).map(member -> {
            try {
                Order order = orderService.checkout(member.getId(), couponId);
                redirectAttributes.addFlashAttribute("successMessage", "訂單處理成功！訂單編號：" + order.getId());
            } catch (RuntimeException e) {
                redirectAttributes.addFlashAttribute("errorMessage", "結帳失敗：" + e.getMessage());
            }
            return "redirect:/orders";
        }).orElse("redirect:/login");
    }

    @PostMapping("/{id}/refund")
    public String refundOrder(@CurrentUser TokenService.TokenInfo info,
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        return getMember(info).map(member -> {
            try {
                orderService.refundOrder(id, member.getId());
                redirectAttributes.addFlashAttribute("successMessage", "訂單 #" + id + " 已申請退貨並退款。");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "退貨失敗：" + e.getMessage());
            }
            return "redirect:/orders";
        }).orElse("redirect:/login");
    }

    @GetMapping
    public String getMyOrders(@CurrentUser TokenService.TokenInfo info, Model model) {
        return getMember(info).map(member -> {
            model.addAttribute("orders", orderService.getMemberOrders(member.getId()));
            return "orders";
        }).orElse("redirect:/login");
    }
}
