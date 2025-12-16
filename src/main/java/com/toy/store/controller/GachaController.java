package com.toy.store.controller;

import com.toy.store.service.SystemSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 抽獎系統前端頁面控制器
 */
@Controller
public class GachaController {

    @Autowired
    private SystemSettingService settingService;

    @GetMapping("/ichiban")
    public String ichibanPage(Model model) {
        if (!settingService.isIchibanEnabled()) {
            return "redirect:/";
        }
        return "ichiban";
    }

    @GetMapping("/roulette")
    public String roulettePage(Model model) {
        if (!settingService.isRouletteEnabled()) {
            return "redirect:/";
        }
        return "roulette";
    }

    @GetMapping("/bingo")
    public String bingoPage(Model model) {
        if (!settingService.isBingoEnabled()) {
            return "redirect:/";
        }
        return "bingo";
    }

    @GetMapping("/redeem-shop")
    public String redeemShopPage(Model model) {
        if (!settingService.isRedeemEnabled()) {
            return "redirect:/";
        }
        return "redeem-shop";
    }
}
