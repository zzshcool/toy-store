package com.toy.store.controller;

import com.toy.store.model.RedeemShopItem;
import com.toy.store.mapper.RedeemShopItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 兌換商城前台控制器
 */
@Controller
@RequestMapping("/redeem-shop")
@RequiredArgsConstructor
public class RedeemShopController {

    private final RedeemShopItemMapper itemMapper;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("items", itemMapper.findByStatusOrderBySortOrderAsc(RedeemShopItem.Status.ACTIVE.name()));
        return "redeem-shop";
    }
}
