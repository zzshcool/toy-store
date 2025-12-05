package com.toy.store.controller;

import com.toy.store.model.MysteryBoxItem;
import com.toy.store.security.services.UserDetailsImpl;
import com.toy.store.service.MysteryBoxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @GetMapping
    public String getAllThemes(Model model) {
        model.addAttribute("themes", mysteryBoxService.getAllThemes());
        return "mystery-box";
    }

    @PostMapping("/draw")
    public String drawBox(@AuthenticationPrincipal UserDetailsImpl user,
            @RequestParam Long themeId,
            RedirectAttributes redirectAttributes) {
        try {
            MysteryBoxItem item = mysteryBoxService.drawBox(user.getId(), themeId);
            redirectAttributes.addFlashAttribute("wonItem", item);

            // Log Action
            memberActionLogRepository.save(new com.toy.store.model.MemberActionLog(
                    user.getId(), user.getUsername(), "DRAW_BOX",
                    "Won item: " + item.getName() + " (Theme ID: " + themeId + ")", true));

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "抽獎失敗：" + e.getMessage());
        }
        return "redirect:/mystery-box";
    }
}
