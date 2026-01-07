package com.toy.store.controller.api;

import com.toy.store.dto.ApiResponse;
import com.toy.store.model.Member;
import com.toy.store.mapper.MemberMapper;
import com.toy.store.service.MemberTagService;
import com.toy.store.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 系統公告後台管理 API
 */
@RestController
@RequestMapping("/api/admin/announcements")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasAuthority('ADMIN_MANAGE')")
public class AnnouncementAdminApiController {

    private final MessageService messageService;
    private final MemberTagService tagService;
    private final MemberMapper memberMapper;

    /**
     * 群發公告給所有會員
     */
    @PostMapping("/broadcast")
    public ApiResponse<Map<String, Object>> broadcast(@RequestBody Map<String, String> request) {
        String title = request.get("title");
        String content = request.get("content");

        if (title == null || title.trim().isEmpty()) {
            return ApiResponse.error("標題必填");
        }
        if (content == null || content.trim().isEmpty()) {
            return ApiResponse.error("內容必填");
        }

        List<Member> allMembers = memberMapper.findAll();
        int count = 0;
        for (Member m : allMembers) {
            messageService.sendSystemMessage(m.getId(), title, content);
            count++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("sentCount", count);
        return ApiResponse.ok(result);
    }

    /**
     * 群發公告給特定標籤的會員
     */
    @PostMapping("/broadcast-by-tag/{tagId}")
    public ApiResponse<Map<String, Object>> broadcastByTag(
            @PathVariable Long tagId,
            @RequestBody Map<String, String> request) {
        String title = request.get("title");
        String content = request.get("content");

        if (title == null || title.trim().isEmpty()) {
            return ApiResponse.error("標題必填");
        }
        if (content == null || content.trim().isEmpty()) {
            return ApiResponse.error("內容必填");
        }

        List<Long> memberIds = tagService.getMembersByTag(tagId);
        int count = 0;
        for (Long memberId : memberIds) {
            messageService.sendSystemMessage(memberId, title, content);
            count++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("sentCount", count);
        result.put("tagId", tagId);
        return ApiResponse.ok(result);
    }

    /**
     * 發送公告給單一會員
     */
    @PostMapping("/send/{memberId}")
    public ApiResponse<Void> sendToMember(
            @PathVariable Long memberId,
            @RequestBody Map<String, String> request) {
        String title = request.get("title");
        String content = request.get("content");

        if (title == null || content == null) {
            return ApiResponse.error("標題和內容必填");
        }

        messageService.sendSystemMessage(memberId, title, content);
        return ApiResponse.ok(null);
    }
}
