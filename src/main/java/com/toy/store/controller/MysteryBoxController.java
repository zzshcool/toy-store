package com.toy.store.controller;

import com.toy.store.annotation.CurrentUser;
import com.toy.store.model.Member;
import com.toy.store.model.MemberActionLog;
import com.toy.store.model.MysteryBoxItem;
import com.toy.store.repository.MemberActionLogRepository;
import com.toy.store.repository.MemberRepository;
import com.toy.store.service.MysteryBoxService;
import com.toy.store.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/mystery-box")
public class MysteryBoxController {

    @Autowired
    private MysteryBoxService mysteryBoxService;

    @Autowired
    private MemberActionLogRepository memberActionLogRepository;

    @Autowired
    private MemberRepository memberRepository;

    @GetMapping
    public String getAllThemes(Model model) {
        model.addAttribute("themes", mysteryBoxService.getAllThemes());
        return "mystery-box";
    }

    @PostMapping("/draw")
    public String drawBox(@CurrentUser TokenService.TokenInfo info,
            @RequestParam Long themeId,
            @RequestParam(required = false) Long couponId,
            @RequestParam(required = false, defaultValue = "false") boolean isTrial,
            RedirectAttributes redirectAttributes) {

        if (isTrial) {
            MysteryBoxItem item = mysteryBoxService.drawTrial(themeId);
            redirectAttributes.addFlashAttribute("wonItem", item);
            redirectAttributes.addFlashAttribute("isTrial", true);
            return "redirect:/mystery-box";
        }

        if (info == null)
            return "redirect:/login";
        Member member = memberRepository.findByUsername(info.getUsername()).orElse(null);
        if (member == null)
            return "redirect:/login";

        MysteryBoxItem item = mysteryBoxService.drawBox(member.getId(), themeId, couponId);
        redirectAttributes.addFlashAttribute("wonItem", item);

        // Log Action
        memberActionLogRepository.save(new MemberActionLog(
                member.getId(), member.getUsername(), "DRAW_BOX",
                "Won item: " + item.getName() + " (Theme ID: " + themeId + ")", true));

        return "redirect:/mystery-box";
    }
}
