package com.toy.store.controller;

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

    @GetMapping
    public String index(Model model) {
        // 前端使用 Alpine.js 的 fetchItems() 載入商品列表，此處無需 SSR 查詢
        return "redeem-shop";
    }
}
