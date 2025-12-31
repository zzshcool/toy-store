package com.toy.store.controller.api;

import com.toy.store.dto.ApiResponse;
import com.toy.store.model.MemberMessage;
import com.toy.store.repository.MemberRepository;
import com.toy.store.service.MessageService;
import com.toy.store.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息通知 API
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageApiController {

    private final MessageService messageService;
    private final MemberRepository memberRepository;

    @GetMapping
    public ApiResponse<List<MemberMessage>> getMessages(HttpServletRequest request) {
        TokenService.TokenInfo user = (TokenService.TokenInfo) request.getAttribute("currentUser");
        if (user == null) {
            return ApiResponse.error("請先登入");
        }

        return memberRepository.findByUsername(user.getUsername())
                .map(member -> ApiResponse.ok(messageService.getMessages(member.getId())))
                .orElse(ApiResponse.error("會員不存在"));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Object>> getUnreadCount(HttpServletRequest request) {
        TokenService.TokenInfo user = (TokenService.TokenInfo) request.getAttribute("currentUser");
        if (user == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("count", 0);
            return ApiResponse.ok(result);
        }

        return memberRepository.findByUsername(user.getUsername())
                .map(member -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("count", messageService.getUnreadCount(member.getId()));
                    return ApiResponse.ok(result);
                })
                .orElseGet(() -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("count", 0);
                    return ApiResponse.ok(result);
                });
    }

    @PostMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(@PathVariable Long id, HttpServletRequest request) {
        TokenService.TokenInfo user = (TokenService.TokenInfo) request.getAttribute("currentUser");
        if (user == null) {
            return ApiResponse.error("請先登入");
        }

        messageService.markAsRead(id);
        return ApiResponse.ok(null, "已標記為已讀");
    }

    @PostMapping("/read-all")
    public ApiResponse<Void> markAllAsRead(HttpServletRequest request) {
        TokenService.TokenInfo user = (TokenService.TokenInfo) request.getAttribute("currentUser");
        if (user == null) {
            return ApiResponse.error("請先登入");
        }

        return memberRepository.findByUsername(user.getUsername())
                .map(member -> {
                    messageService.markAllAsRead(member.getId());
                    return ApiResponse.<Void>ok(null, "已全部標記為已讀");
                })
                .orElse(ApiResponse.error("會員不存在"));
    }
}
