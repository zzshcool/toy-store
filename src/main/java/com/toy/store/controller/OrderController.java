package com.toy.store.controller;

import com.toy.store.model.Order;
import com.toy.store.security.services.UserDetailsImpl;
import com.toy.store.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/checkout")
    public String checkout(@AuthenticationPrincipal UserDetailsImpl user, RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.checkout(user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "訂單處理成功！訂單編號：" + order.getId());
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "結帳失敗：" + e.getMessage());
        }
        return "redirect:/orders";
    }

    @PostMapping("/{id}/refund")
    public String refundOrder(@AuthenticationPrincipal UserDetailsImpl user,
            @org.springframework.web.bind.annotation.PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            // Logic should be in Service, but for simplicity adding here or calling service
            // Assuming OrderService has a refund method or we implement it now
            orderService.refundOrder(id, user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "訂單 #" + id + " 已申請退貨並退款。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "退貨失敗：" + e.getMessage());
        }
        return "redirect:/orders";
    }

    @GetMapping
    public String getMyOrders(@AuthenticationPrincipal UserDetailsImpl user, Model model) {
        model.addAttribute("orders", orderService.getMemberOrders(user.getId()));
        return "orders";
    }
}
