package com.toy.store.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 轉盤前台控制器
 */
@Controller
@RequestMapping("/roulette")
public class RouletteController {

    @GetMapping
    public String index(Model model) {
        // 前端使用 Alpine.js 的 fetchGames() 載入遊戲列表，此處無需 SSR 查詢
        return "roulette";
    }
}
