package com.toy.store.controller;

import com.toy.store.repository.RouletteGameRepository;
import com.toy.store.model.RouletteGame;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private RouletteGameRepository gameRepository;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("games", gameRepository.findByStatus(RouletteGame.Status.ACTIVE));
        return "roulette";
    }
}
