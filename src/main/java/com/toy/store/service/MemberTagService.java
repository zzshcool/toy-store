package com.toy.store.service;

import com.toy.store.model.*;
import com.toy.store.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 會員標籤服務
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberTagService {

    private final MemberTagRepository tagRepository;
    private final MemberTagRelationRepository relationRepository;

    /**
     * 創建標籤
     */
    @Transactional
    public MemberTag createTag(String name, String description, String color, MemberTag.TagType type) {
        MemberTag tag = new MemberTag();
        tag.setName(name);
        tag.setDescription(description);
        tag.setColor(color);
        tag.setType(type);
        return tagRepository.save(tag);
    }

    /**
     * 給會員添加標籤
     */
    @Transactional
    public void addTagToMember(Long memberId, Long tagId, String source) {
        if (relationRepository.existsByMemberIdAndTagId(memberId, tagId)) {
            return; // 已有此標籤
        }

        tagRepository.findById(tagId).ifPresent(tag -> {
            MemberTagRelation relation = new MemberTagRelation();
            relation.setMemberId(memberId);
            relation.setTag(tag);
            relation.setSource(source);
            relationRepository.save(relation);
            log.info("會員 {} 添加標籤 {}", memberId, tag.getName());
        });
    }

    /**
     * 移除會員標籤
     */
    @Transactional
    public void removeTagFromMember(Long memberId, Long tagId) {
        relationRepository.deleteByMemberIdAndTagId(memberId, tagId);
    }

    /**
     * 獲取會員的所有標籤
     */
    public List<MemberTag> getMemberTags(Long memberId) {
        return relationRepository.findByMemberId(memberId)
                .stream()
                .map(MemberTagRelation::getTag)
                .collect(Collectors.toList());
    }

    /**
     * 獲取擁有特定標籤的所有會員ID
     */
    public List<Long> getMembersByTag(Long tagId) {
        return relationRepository.findByTagId(tagId)
                .stream()
                .map(MemberTagRelation::getMemberId)
                .collect(Collectors.toList());
    }

    /**
     * 初始化系統標籤
     */
    @Transactional
    public void initSystemTags() {
        createTagIfNotExists("新會員", "註冊7天內", "#4CAF50", MemberTag.TagType.SYSTEM);
        createTagIfNotExists("活躍用戶", "30天內有消費", "#2196F3", MemberTag.TagType.SYSTEM);
        createTagIfNotExists("高價值用戶", "累計消費>$10000", "#FF9800", MemberTag.TagType.SYSTEM);
        createTagIfNotExists("流失風險", "60天未登入", "#F44336", MemberTag.TagType.SYSTEM);
        createTagIfNotExists("VIP客戶", "VIP5及以上", "#9C27B0", MemberTag.TagType.SYSTEM);
    }

    private void createTagIfNotExists(String name, String desc, String color, MemberTag.TagType type) {
        if (tagRepository.findByName(name).isEmpty()) {
            createTag(name, desc, color, type);
        }
    }

    /**
     * 獲取所有標籤
     */
    public List<MemberTag> getAllTags() {
        return tagRepository.findAll();
    }
}
