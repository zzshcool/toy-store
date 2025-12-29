package com.toy.store.controller;

import com.toy.store.annotation.CurrentUser;
import com.toy.store.repository.MemberRepository;
import com.toy.store.service.CabinetService;
import com.toy.store.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 盒櫃頁面 Controller
 */
@Controller
@RequestMapping("/cabinet")
@RequiredArgsConstructor
public class CabinetController {

    private final CabinetService cabinetService;
    private final MemberRepository memberRepository;

    @GetMapping
    public String cabinetPage(Model model, @CurrentUser TokenService.TokenInfo info) {
        if (info != null) {
            memberRepository.findByUsername(info.getUsername()).ifPresent(member -> {
                int count = cabinetService.getCabinetCount(member.getId());
                model.addAttribute("cabinetCount", count);
            });
        }
        return "cabinet";
    }
}
