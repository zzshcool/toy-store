package com.toy.store.dto.member;

import com.toy.store.model.Member;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 登入回應 DTO
 */
@Data
public class LoginResponseDto {
    private String username;
    private String nickname;
    private BigDecimal balance;
    private String token;

    public static LoginResponseDto from(Member member, String token) {
        LoginResponseDto dto = new LoginResponseDto();
        dto.setUsername(member.getUsername());
        dto.setNickname(member.getNickname());
        dto.setBalance(member.getPlatformWalletBalance());
        dto.setToken(token);
        return dto;
    }
}
