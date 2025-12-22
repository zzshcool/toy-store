package com.toy.store.controller;

import com.toy.store.service.IchibanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 一番賞前台控制器
 */
@Controller
@RequestMapping("/ichiban")
public class IchibanController {

    @Autowired
    private IchibanService ichibanService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("boxes", ichibanService.getActiveBoxes());
        return "ichiban";
    }
}
