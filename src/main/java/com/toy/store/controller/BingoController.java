package com.toy.store.controller;

import com.toy.store.model.BingoGame;
import com.toy.store.repository.BingoGameRepository;
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

    private final BingoGameRepository gameRepository;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("games", gameRepository.findByStatus(BingoGame.Status.ACTIVE));
        return "bingo";
    }
}
