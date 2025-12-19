package com.toy.store.controller;

import com.toy.store.annotation.CurrentUser;
import com.toy.store.model.Member;
import com.toy.store.model.Order;
import com.toy.store.service.OrderService;
import com.toy.store.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private com.toy.store.repository.MemberRepository memberRepository;

    @PostMapping("/checkout")
    public String checkout(@CurrentUser TokenService.TokenInfo info,
            @RequestParam(required = false) Long couponId,
            RedirectAttributes redirectAttributes) {
        Member member = getMember(info);
        if (member == null) {
            return "redirect:/login";
        }

        try {
            Order order = orderService.checkout(member.getId(), couponId);
            redirectAttributes.addFlashAttribute("successMessage", "訂單處理成功！訂單編號：" + order.getId());
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "結帳失敗：" + e.getMessage());
        }
        return "redirect:/orders";
    }

    @PostMapping("/{id}/refund")
    public String refundOrder(@CurrentUser TokenService.TokenInfo info,
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        Member member = getMember(info);
        if (member == null)
            return "redirect:/login";
        try {
            orderService.refundOrder(id, member.getId());
            redirectAttributes.addFlashAttribute("successMessage", "訂單 #" + id + " 已申請退貨並退款。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "退貨失敗：" + e.getMessage());
        }
        return "redirect:/orders";
    }

    @GetMapping
    public String getMyOrders(@CurrentUser TokenService.TokenInfo info, Model model) {
        Member member = getMember(info);
        if (member == null)
            return "redirect:/login";

        model.addAttribute("orders", orderService.getMemberOrders(member.getId()));
        return "orders";
    }

    private Member getMember(TokenService.TokenInfo info) {
        if (info == null)
            return null;
        return memberRepository.findByUsername(info.getUsername()).orElse(null);
    }
}
