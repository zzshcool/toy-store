package com.toy.store.controller.api;

import com.toy.store.annotation.CurrentUser;
import com.toy.store.dto.ApiResponse;
import com.toy.store.exception.AppException;
import com.toy.store.model.GachaItem;
import com.toy.store.model.GachaTheme;
import com.toy.store.model.Member;
import com.toy.store.mapper.MemberMapper;
import com.toy.store.service.GachaService;
import com.toy.store.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gacha")
@RequiredArgsConstructor
public class GachaApiController {

    private final GachaService gachaService;
    private final MemberMapper memberMapper;

    @GetMapping("/themes")
    public ApiResponse<List<GachaTheme>> getThemes() {
        return ApiResponse.ok(gachaService.getAllThemes());
    }

    /**
     * 正式抽獎
     */
    @PostMapping("/{id}/draw")
    public ApiResponse<Map<String, Object>> draw(
            @PathVariable Long id,
            @CurrentUser TokenService.TokenInfo info) {

        Long memberId = getMemberId(info);
        if (memberId == null) {
            throw new AppException("請先登入");
        }

        GachaItem item = gachaService.drawBox(memberId, id, null);

        Map<String, Object> response = new HashMap<>();
        response.put("prize", itemToMap(item));
        response.put("isTrial", false);

        return ApiResponse.ok(response, "🎉 恭喜獲得：" + item.getName());
    }

    /**
     * 試抽（免費體驗）
     */
    @PostMapping("/{id}/trial")
    public ApiResponse<Map<String, Object>> trial(@PathVariable Long id) {
        GachaItem item = gachaService.drawTrial(id);

        Map<String, Object> response = new HashMap<>();
        response.put("prize", itemToMap(item));
        response.put("isTrial", true);
        response.put("message", "這是試抽結果，正式抽獎需要登入並使用代幣");

        return ApiResponse.ok(response, "試抽完成！體驗轉蛋的樂趣～");
    }

    private Long getMemberId(TokenService.TokenInfo info) {
        if (info == null)
            return null;
        return memberMapper.findByUsername(info.getUsername())
                .map(Member::getId)
                .orElse(null);
    }

    private Map<String, Object> itemToMap(GachaItem item) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", item.getId());
        map.put("name", item.getName());
        map.put("description", item.getDescription());
        map.put("imageUrl", item.getImageUrl());
        map.put("weight", item.getWeight());
        map.put("value", item.getEstimatedValue());
        return map;
    }
}
