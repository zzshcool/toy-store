package com.toy.store.controller;

import com.toy.store.model.BingoGame;
import com.toy.store.mapper.BingoGameMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 九宮格前台控制器
 */
@Controller
@RequestMapping("/bingo")
@RequiredArgsConstructor
public class BingoController {

    private final BingoGameMapper gameMapper;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("games", gameMapper.findByStatus(BingoGame.Status.ACTIVE.name()));
        return "bingo";
    }
}
