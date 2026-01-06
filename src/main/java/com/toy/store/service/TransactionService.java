package com.toy.store.service;

import com.toy.store.exception.AppException;
import com.toy.store.model.Member;
import com.toy.store.model.MemberLevel;
import com.toy.store.model.MemberMission;
import com.toy.store.model.Transaction;
import com.toy.store.mapper.MemberMapper;
import com.toy.store.mapper.MemberLevelMapper;
import com.toy.store.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    private final MemberMapper memberMapper;
    private final TransactionMapper transactionMapper;
    private final MemberLevelMapper memberLevelMapper;
    private final MissionService missionService;

    public TransactionService(
            MemberMapper memberMapper,
            TransactionMapper transactionMapper,
            MemberLevelMapper memberLevelMapper,
            @Lazy MissionService missionService) {
        this.memberMapper = memberMapper;
        this.transactionMapper = transactionMapper;
        this.memberLevelMapper = memberLevelMapper;
        this.missionService = missionService;
    }

    /**
     * Updates member wallet balance atomically.
     * 
     * @param memberId Member ID
     * @param amount   Amount to add (positive) or subtract (negative)
     * @param type     Transaction Type
     * @throws RuntimeException if member not found or insufficient funds
     */
    @Transactional
    public void updateWalletBalance(Long memberId, BigDecimal amount, Transaction.Type type, String refId) {
        Member member = memberMapper.findById(memberId)
                .orElseThrow(() -> new AppException("會員不存在"));

        BigDecimal newBalance = member.getPlatformWalletBalance().add(amount);

        // Check for sufficient funds if subtracting
        if (amount.compareTo(BigDecimal.ZERO) < 0 && newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new AppException("餘額不足，無法完成操作");
        }

        // Handle consumption (negative amount) - Award Growth Value 1:1
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            long spent = amount.abs().longValue();
            member.setGrowthValue(member.getGrowthValue() + spent);
            checkAndUpgradeLevel(member);

            // 觸發消費任務
            try {
                missionService.updateMissionProgress(memberId,
                        MemberMission.MissionType.SPEND_AMOUNT, (int) spent);
            } catch (Exception e) {
                // Log and continue
            }
        }

        if (type == Transaction.Type.RECHARGE) {
            member.setMonthlyRecharge(member.getMonthlyRecharge().add(amount));
            checkAndUpgradeLevel(member);
        }

        member.setPlatformWalletBalance(newBalance);
        memberMapper.update(member);

        Transaction transaction = new Transaction();
        transaction.setMemberId(memberId);
        transaction.setAmount(amount);
        transaction.setType(type.name());
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setDescription(refId);

        transactionMapper.insert(transaction);
    }

    /**
     * 扣除餘額（便捷方法）
     */
    @Transactional
    public void deductBalance(Long memberId, BigDecimal amount, String description) {
        updateWalletBalance(memberId, amount.negate(), Transaction.Type.PURCHASE, description);
    }

    /**
     * 增加餘額（便捷方法）
     */
    @Transactional
    public void addBalance(Long memberId, BigDecimal amount, String description) {
        updateWalletBalance(memberId, amount, Transaction.Type.REFUND, description);
    }

    private void checkAndUpgradeLevel(Member member) {
        Long memberLevelId = member.getMemberLevelId();

        // Fetch all levels sorted by min growth value
        List<MemberLevel> allLevels = memberLevelMapper.findAll();

        if (allLevels.isEmpty())
            return;

        // 新會員無等級時，或重新指派
        MemberLevel current = null;
        if (memberLevelId != null) {
            current = memberLevelMapper.findById(memberLevelId).orElse(null);
        }

        if (current == null) {
            member.setMemberLevelId(allLevels.get(0).getId());
            current = allLevels.get(0);
        }

        MemberLevel targetLevel = current;

        // Find the highest eligible level using growthValue
        Long growthValue = member.getGrowthValue();
        for (MemberLevel level : allLevels) {
            if (level.getMinGrowthValue() != null &&
                    growthValue >= level.getMinGrowthValue()) {
                targetLevel = level;
            }
        }

        if (!targetLevel.getId().equals(current.getId())) {
            member.setMemberLevelId(targetLevel.getId());
            member.setLastLevelReviewDate(LocalDate.now());
        }
    }

    /**
     * 查詢會員交易記錄
     */
    public List<Transaction> findByMemberId(Long memberId) {
        return transactionMapper.findByMemberId(memberId);
    }

    /**
     * 查詢所有交易記錄
     */
    public List<Transaction> findAll() {
        return transactionMapper.findAll();
    }
}
