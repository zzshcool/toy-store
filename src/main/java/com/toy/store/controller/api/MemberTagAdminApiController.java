package com.toy.store.controller.api;

import com.toy.store.dto.ApiResponse;
import com.toy.store.model.MemberTag;
import com.toy.store.service.MemberTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 會員標籤後台管理 API
 */
@RestController
@RequestMapping("/api/admin/member-tags")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasAuthority('MEMBER_MANAGE')")
public class MemberTagAdminApiController {

    private final MemberTagService tagService;

    /**
     * 取得所有標籤
     */
    @GetMapping
    public ApiResponse<List<Map<String, Object>>> getAllTags() {
        List<MemberTag> tags = tagService.getAllTags();
        List<Map<String, Object>> result = tags.stream()
                .map(this::mapTag)
                .collect(Collectors.toList());
        return ApiResponse.ok(result);
    }

    /**
     * 建立標籤
     */
    @PostMapping
    public ApiResponse<Map<String, Object>> createTag(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String description = request.get("description");
        String color = request.get("color");
        String typeStr = request.get("type");

        if (name == null || name.trim().isEmpty()) {
            return ApiResponse.error("標籤名稱必填");
        }

        MemberTag.TagType type = MemberTag.TagType.MANUAL;
        if (typeStr != null) {
            try {
                type = MemberTag.TagType.valueOf(typeStr);
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (color == null || color.isEmpty()) {
            color = "#607D8B"; // 預設灰色
        }

        MemberTag tag = tagService.createTag(name, description, color, type);
        return ApiResponse.ok(mapTag(tag));
    }

    /**
     * 給會員添加標籤
     */
    @PostMapping("/{tagId}/members/{memberId}")
    public ApiResponse<Void> addTagToMember(
            @PathVariable Long tagId,
            @PathVariable Long memberId) {
        tagService.addTagToMember(memberId, tagId, "ADMIN");
        return ApiResponse.ok(null);
    }

    /**
     * 移除會員標籤
     */
    @DeleteMapping("/{tagId}/members/{memberId}")
    public ApiResponse<Void> removeTagFromMember(
            @PathVariable Long tagId,
            @PathVariable Long memberId) {
        tagService.removeTagFromMember(memberId, tagId);
        return ApiResponse.ok(null);
    }

    /**
     * 取得擁有特定標籤的會員 ID 列表
     */
    @GetMapping("/{tagId}/members")
    public ApiResponse<List<Long>> getMembersByTag(@PathVariable Long tagId) {
        List<Long> memberIds = tagService.getMembersByTag(tagId);
        return ApiResponse.ok(memberIds);
    }

    /**
     * 批量給會員添加標籤
     */
    @PostMapping("/{tagId}/members/batch")
    public ApiResponse<Map<String, Object>> batchAddTag(
            @PathVariable Long tagId,
            @RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Number> memberIds = (List<Number>) request.get("memberIds");
        if (memberIds == null || memberIds.isEmpty()) {
            return ApiResponse.error("會員 ID 列表必填");
        }

        int count = 0;
        for (Number id : memberIds) {
            tagService.addTagToMember(id.longValue(), tagId, "ADMIN_BATCH");
            count++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("taggedCount", count);
        return ApiResponse.ok(result);
    }

    private Map<String, Object> mapTag(MemberTag tag) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", tag.getId());
        map.put("name", tag.getName());
        map.put("description", tag.getDescription());
        map.put("color", tag.getColor());
        map.put("type", tag.getType().name());
        return map;
    }
}
