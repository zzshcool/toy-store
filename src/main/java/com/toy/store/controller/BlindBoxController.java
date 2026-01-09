package com.toy.store.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 盲盒（動漫周邊）頁面 Controller
 */
@Controller
@RequestMapping("/blindbox")
public class BlindBoxController {

    @GetMapping
    public String blindBoxPage(Model model) {
        // 前端使用 Alpine.js 的 fetchBoxes() 載入盲盒列表，此處無需 SSR 查詢
        return "blindbox";
    }
}
