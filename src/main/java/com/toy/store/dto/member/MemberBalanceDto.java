package com.toy.store.dto.member;

import com.toy.store.model.Member;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 會員餘額 DTO
 */
@Data
public class MemberBalanceDto {
    private BigDecimal balance;
    private String levelName;
    private int shardBalance;

    public static MemberBalanceDto from(Member member) {
        MemberBalanceDto dto = new MemberBalanceDto();
        dto.setBalance(member.getPlatformWalletBalance());
        dto.setLevelName(member.getLevel() != null ? member.getLevel().getName() : "一般會員");
        dto.setShardBalance(member.getPoints());
        return dto;
    }
}
