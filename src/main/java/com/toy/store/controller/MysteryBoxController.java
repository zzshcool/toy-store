package com.toy.store.controller;

import com.toy.store.model.Member;
import com.toy.store.model.MysteryBoxItem;
import com.toy.store.repository.MemberRepository;
import com.toy.store.service.MysteryBoxService;
import com.toy.store.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
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
    private com.toy.store.repository.MemberActionLogRepository memberActionLogRepository;

    @Autowired
    private MemberRepository memberRepository;

    @GetMapping
    public String getAllThemes(Model model) {
        model.addAttribute("themes", mysteryBoxService.getAllThemes());
        return "mystery-box";
    }

    @PostMapping("/draw")
    public String drawBox(HttpServletRequest request,
            @RequestParam Long themeId,
            RedirectAttributes redirectAttributes) {

        TokenService.TokenInfo info = (TokenService.TokenInfo) request.getAttribute("currentUser");
        if (info == null)
            return "redirect:/login";
        Member member = memberRepository.findByUsername(info.getUsername()).orElse(null);
        if (member == null)
            return "redirect:/login";

        try {
            MysteryBoxItem item = mysteryBoxService.drawBox(member.getId(), themeId);
            redirectAttributes.addFlashAttribute("wonItem", item);

            // Log Action
            memberActionLogRepository.save(new com.toy.store.model.MemberActionLog(
                    member.getId(), member.getUsername(), "DRAW_BOX",
                    "Won item: " + item.getName() + " (Theme ID: " + themeId + ")", true));

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "抽獎失敗：" + e.getMessage());
        }
        return "redirect:/mystery-box";
    }
}
