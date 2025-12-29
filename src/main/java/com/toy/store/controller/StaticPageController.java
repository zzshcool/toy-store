package com.toy.store.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 靜態頁面控制器
 * 處理關於我們、FAQ、聯絡我們等靜態頁面
 */
@Controller
public class StaticPageController {

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/faq")
    public String faq() {
        return "faq";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @GetMapping("/messages")
    public String messages() {
        return "messages";
    }

    @GetMapping("/verification")
    public String verification() {
        return "verification";
    }
}
