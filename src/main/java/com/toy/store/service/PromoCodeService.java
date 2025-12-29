package com.toy.store.service;

import com.toy.store.model.*;
import com.toy.store.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 推薦碼/禮包碼服務
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromoCodeService {

    private final PromoCodeRepository promoCodeRepository;
    private final PromoCodeUsageRepository usageRepository;
    private final MemberRepository memberRepository;

    private final SecureRandom random = new SecureRandom();

    /**
     * 生成用戶專屬推薦碼
     */
    @Transactional
    public PromoCode generateReferralCode(Long memberId) {
        // 檢查是否已有推薦碼
        Optional<PromoCode> existing = promoCodeRepository.findByCreatorMemberId(memberId)
                .stream().filter(c -> c.getType() == PromoCode.CodeType.REFERRAL).findFirst();
        if (existing.isPresent()) {
            return existing.get();
        }

        PromoCode code = new PromoCode();
        code.setCode(generateUniqueCode("REF"));
        code.setName("用戶推薦碼");
        code.setDescription("邀請好友註冊，雙方各得獎勵");
        code.setType(PromoCode.CodeType.REFERRAL);
        code.setRewardType(PromoCode.RewardType.TOKENS);
        code.setRewardValue(BigDecimal.valueOf(50)); // 50 代幣
        code.setMaxUses(0); // 無限
        code.setPerUserLimit(1);
        code.setCreatorMemberId(memberId);

        return promoCodeRepository.save(code);
    }

    /**
     * 創建禮包碼（後台使用）
     */
    @Transactional
    public PromoCode createGiftCode(String name, String description,
            PromoCode.RewardType rewardType, BigDecimal rewardValue,
            int maxUses, LocalDateTime validUntil) {

        PromoCode code = new PromoCode();
        code.setCode(generateUniqueCode("GIFT"));
        code.setName(name);
        code.setDescription(description);
        code.setType(PromoCode.CodeType.GIFT);
        code.setRewardType(rewardType);
        code.setRewardValue(rewardValue);
        code.setMaxUses(maxUses);
        code.setPerUserLimit(1);
        code.setValidUntil(validUntil);

        return promoCodeRepository.save(code);
    }

    /**
     * 兌換推薦碼/禮包碼
     */
    @Transactional
    public String redeemCode(Long memberId, String codeStr) {
        PromoCode code = promoCodeRepository.findByCode(codeStr.toUpperCase()).orElse(null);
        if (code == null) {
            return "無效的兌換碼";
        }

        // 驗證有效性
        if (!code.isValid()) {
            return "兌換碼已過期或已用完";
        }

        // 檢查是否為自己的推薦碼
        if (code.getCreatorMemberId() != null && code.getCreatorMemberId().equals(memberId)) {
            return "不能使用自己的推薦碼";
        }

        // 檢查使用次數
        long userUsageCount = usageRepository.countByPromoCodeIdAndMemberId(code.getId(), memberId);
        if (userUsageCount >= code.getPerUserLimit()) {
            return "您已達到此兌換碼使用上限";
        }

        // 發放獎勵
        Member member = memberRepository.findById(memberId).orElse(null);
        if (member == null) {
            return "會員不存在";
        }

        String rewardDesc = applyReward(member, code);

        // 記錄使用
        PromoCodeUsage usage = new PromoCodeUsage();
        usage.setPromoCode(code);
        usage.setMemberId(memberId);
        usageRepository.save(usage);

        // 更新使用次數
        code.setUsedCount(code.getUsedCount() + 1);
        promoCodeRepository.save(code);

        // 如果是推薦碼，給推薦人也發獎勵
        if (code.getType() == PromoCode.CodeType.REFERRAL && code.getCreatorMemberId() != null) {
            memberRepository.findById(code.getCreatorMemberId()).ifPresent(referrer -> {
                applyReward(referrer, code);
                log.info("推薦人 {} 獲得推薦獎勵", referrer.getUsername());
            });
        }

        log.info("會員 {} 兌換碼 {} 成功: {}", memberId, codeStr, rewardDesc);
        return "兌換成功！" + rewardDesc;
    }

    /**
     * 發放獎勵
     */
    private String applyReward(Member member, PromoCode code) {
        switch (code.getRewardType()) {
            case TOKENS:
                member.setPlatformWalletBalance(
                        member.getPlatformWalletBalance().add(code.getRewardValue()));
                memberRepository.save(member);
                return "獲得 " + code.getRewardValue().intValue() + " 代幣";

            case BONUS:
                int bonus = code.getRewardValue().intValue();
                member.setBonusPoints((member.getBonusPoints() != null ? member.getBonusPoints() : 0) + bonus);
                memberRepository.save(member);
                return "獲得 " + bonus + " 紅利點數";

            case SHARDS:
                // 簡化處理
                return "獲得 " + code.getRewardValue().intValue() + " 碎片";

            default:
                return "獲得獎勵";
        }
    }

    /**
     * 生成唯一碼
     */
    private String generateUniqueCode(String prefix) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(prefix);
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        String code = sb.toString();

        // 確保唯一
        if (promoCodeRepository.existsByCode(code)) {
            return generateUniqueCode(prefix);
        }
        return code;
    }

    /**
     * 獲取用戶的推薦碼
     */
    public Optional<PromoCode> getMemberReferralCode(Long memberId) {
        return promoCodeRepository.findByCreatorMemberId(memberId)
                .stream().filter(c -> c.getType() == PromoCode.CodeType.REFERRAL).findFirst();
    }
}
