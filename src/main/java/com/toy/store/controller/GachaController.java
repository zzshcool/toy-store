package com.toy.store.controller;

import com.toy.store.annotation.CurrentUser;
import com.toy.store.model.Member;
import com.toy.store.model.MemberActionLog;
import com.toy.store.model.GachaItem;
import com.toy.store.repository.MemberActionLogRepository;
import com.toy.store.repository.MemberRepository;
import com.toy.store.service.GachaService;
import com.toy.store.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/gacha")
public class GachaController {

    @Autowired
    private GachaService gachaService;

    @Autowired
    private MemberActionLogRepository memberActionLogRepository;

    @Autowired
    private MemberRepository memberRepository;

    @GetMapping
    public String getAllThemes(Model model) {
        model.addAttribute("themes", gachaService.getAllThemes());
        return "gacha"; // Template will be renamed too
    }

    @PostMapping("/draw")
    @ResponseBody
    public org.springframework.http.ResponseEntity<java.util.Map<String, Object>> drawBox(
            @CurrentUser TokenService.TokenInfo info,
            @RequestParam Long themeId,
            @RequestParam(required = false) Long couponId,
            @RequestParam(required = false, defaultValue = "false") boolean isTrial) {

        java.util.Map<String, Object> response = new java.util.LinkedHashMap<>();
        try {
            if (isTrial) {
                GachaItem item = gachaService.drawTrial(themeId);
                response.put("success", true);
                response.put("wonItem", item);
                response.put("isTrial", true);
                return org.springframework.http.ResponseEntity.ok(response);
            }

            if (info == null) {
                response.put("success", false);
                response.put("message", "請先登入 (Please login first)");
                return org.springframework.http.ResponseEntity.status(401).body(response);
            }

            Member member = memberRepository.findByUsername(info.getUsername()).orElse(null);
            if (member == null) {
                response.put("success", false);
                response.put("message", "用戶不存在 (User not found)");
                return org.springframework.http.ResponseEntity.status(404).body(response);
            }

            GachaItem item = gachaService.drawBox(member.getId(), themeId, couponId);
            response.put("success", true);
            response.put("wonItem", item);

            // Log Action
            memberActionLogRepository.save(new MemberActionLog(
                    member.getId(), member.getUsername(), "DRAW_GACHA",
                    "Won item: " + item.getName() + " (Theme ID: " + themeId + ")", true));

            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return org.springframework.http.ResponseEntity.status(500).body(response);
        }
    }
}
