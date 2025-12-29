package com.toy.store.controller;

import com.toy.store.service.BlindBoxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 盲盒（動漫周邊）頁面 Controller
 */
@Controller
@RequestMapping("/blindbox")
@RequiredArgsConstructor
public class BlindBoxController {

    private final BlindBoxService blindBoxService;

    @GetMapping
    public String blindBoxPage(Model model) {
        model.addAttribute("boxes", blindBoxService.getActiveBoxes());
        return "blindbox";
    }
}
