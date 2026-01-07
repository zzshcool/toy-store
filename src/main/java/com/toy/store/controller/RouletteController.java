package com.toy.store.controller;

import com.toy.store.model.RouletteGame;
import com.toy.store.mapper.RouletteGameMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 轉盤前台控制器
 */
@Controller
@RequestMapping("/roulette")
@RequiredArgsConstructor
public class RouletteController {

    private final RouletteGameMapper gameMapper;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("games", gameMapper.findByStatus(RouletteGame.Status.ACTIVE.name()));
        return "roulette";
    }
}
