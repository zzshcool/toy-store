package com.toy.store.controller;

import com.toy.store.annotation.CurrentUser;
import com.toy.store.model.GachaItem;
import com.toy.store.model.MemberActionLog;
import com.toy.store.repository.MemberActionLogRepository;
import com.toy.store.repository.MemberRepository;
import com.toy.store.service.GachaService;
import com.toy.store.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/gacha")
@RequiredArgsConstructor
public class GachaController {

    private final GachaService gachaService;
    private final MemberActionLogRepository memberActionLogRepository;
    private final MemberRepository memberRepository;

    @GetMapping
    public String getAllThemes(Model model) {
        model.addAttribute("themes", gachaService.getAllThemes());
        return "gacha";
    }

    @PostMapping("/draw")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> drawBox(
            @CurrentUser TokenService.TokenInfo info,
            @RequestParam Long themeId,
            @RequestParam(required = false) Long couponId,
            @RequestParam(required = false, defaultValue = "false") boolean isTrial) {

        Map<String, Object> response = new LinkedHashMap<>();
        try {
            if (isTrial) {
                GachaItem item = gachaService.drawTrial(themeId);
                response.put("success", true);
                response.put("wonItem", item);
                response.put("isTrial", true);
                return ResponseEntity.ok(response);
            }

            if (info == null) {
                response.put("success", false);
                response.put("message", "請先登入 (Please login first)");
                return ResponseEntity.status(401).body(response);
            }

            return memberRepository.findByUsername(info.getUsername())
                    .map(member -> {
                        GachaItem item = gachaService.drawBox(member.getId(), themeId, couponId);
                        response.put("success", true);
                        response.put("wonItem", item);

                        // Log Action
                        memberActionLogRepository.save(new MemberActionLog(
                                member.getId(), member.getUsername(), "DRAW_GACHA",
                                "Won item: " + item.getName() + " (Theme ID: " + themeId + ")", true));

                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        response.put("success", false);
                        response.put("message", "用戶不存在 (User not found)");
                        return ResponseEntity.status(404).body(response);
                    });
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
