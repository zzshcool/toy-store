package com.toy.store.controller;

import com.toy.store.repository.RedeemShopItemRepository;
import com.toy.store.model.RedeemShopItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 兌換商城前台控制器
 */
@Controller
@RequestMapping("/redeem-shop")
public class RedeemShopController {

    @Autowired
    private RedeemShopItemRepository itemRepository;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("items", itemRepository.findByStatusOrderBySortOrderAsc(RedeemShopItem.Status.ACTIVE));
        return "redeem-shop";
    }
}
