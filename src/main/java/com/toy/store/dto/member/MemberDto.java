package com.toy.store.dto.member;

import com.toy.store.model.Member;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 會員資訊 DTO
 */
@Data
public class MemberDto {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String avatarUrl;
    private String levelName;
    private BigDecimal balance;
    private int points;
    private int luckyValue;
    private long growthValue;
    private LocalDateTime createdAt;

    public static MemberDto from(Member member) {
        MemberDto dto = new MemberDto();
        dto.setId(member.getId());
        dto.setUsername(member.getUsername());
        dto.setNickname(member.getNickname());
        dto.setEmail(member.getEmail());
        dto.setAvatarUrl(member.getAvatarUrl());
        dto.setLevelName(member.getLevel() != null ? member.getLevel().getName() : "一般會員");
        dto.setBalance(member.getPlatformWalletBalance());
        dto.setPoints(member.getPoints());
        dto.setLuckyValue(member.getLuckyValue());
        dto.setGrowthValue(member.getGrowthValue());
        dto.setCreatedAt(member.getCreatedAt());
        return dto;
    }
}
