package com.toy.store.controller;

import com.toy.store.model.Order;
import com.toy.store.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private com.toy.store.repository.MemberRepository memberRepository;

    @Autowired
    private com.toy.store.service.TokenService tokenService;

    @PostMapping("/checkout")
    public String checkout(jakarta.servlet.http.HttpServletRequest request,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Long couponId,
            RedirectAttributes redirectAttributes) {
        com.toy.store.service.TokenService.TokenInfo info = (com.toy.store.service.TokenService.TokenInfo) request
                .getAttribute("currentUser");
        if (info == null) {
            return "redirect:/login";
        }
        com.toy.store.model.Member member = memberRepository.findByUsername(info.getUsername()).orElse(null);
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
    public String refundOrder(jakarta.servlet.http.HttpServletRequest request,
            @org.springframework.web.bind.annotation.PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        com.toy.store.service.TokenService.TokenInfo info = (com.toy.store.service.TokenService.TokenInfo) request
                .getAttribute("currentUser");
        if (info == null)
            return "redirect:/login";
        com.toy.store.model.Member member = memberRepository.findByUsername(info.getUsername()).orElse(null);
        if (member == null)
            return "redirect:/login";
        try {
            // Logic should be in Service, but for simplicity adding here or calling service
            // Assuming OrderService has a refund method or we implement it now
            orderService.refundOrder(id, member.getId());
            redirectAttributes.addFlashAttribute("successMessage", "訂單 #" + id + " 已申請退貨並退款。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "退貨失敗：" + e.getMessage());
        }
        return "redirect:/orders";
    }

    @GetMapping
    public String getMyOrders(jakarta.servlet.http.HttpServletRequest request, Model model) {
        com.toy.store.service.TokenService.TokenInfo info = (com.toy.store.service.TokenService.TokenInfo) request
                .getAttribute("currentUser");
        if (info == null)
            return "redirect:/login";
        com.toy.store.model.Member member = memberRepository.findByUsername(info.getUsername()).orElse(null);
        if (member == null)
            return "redirect:/login";

        model.addAttribute("orders", orderService.getMemberOrders(member.getId()));
        return "orders";
    }
}
