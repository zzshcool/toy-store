package com.toy.store.service;

import com.toy.store.exception.AppException;
import com.toy.store.model.Member;
import com.toy.store.model.MemberLevel;
import com.toy.store.model.MemberMission;
import com.toy.store.model.Transaction;
import com.toy.store.repository.MemberLevelRepository;
import com.toy.store.repository.MemberRepository;
import com.toy.store.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final MemberRepository memberRepository;
    private final TransactionRepository transactionRepository;
    private final MemberLevelRepository memberLevelRepository;
    @Lazy
    private final MissionService missionService;

    /**
     * Updates member wallet balance atomically.
     * 
     * @param memberId Member ID
     * @param amount   Amount to add (positive) or subtract (negative)
     * @param type     Transaction Type
     * @throws RuntimeException if member not found or insufficient funds
     */
    @Transactional
    public void updateWalletBalance(Long memberId, BigDecimal amount, Transaction.TransactionType type, String refId) {
        Member member = memberRepository.findById(memberId)
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

        if (type == Transaction.TransactionType.DEPOSIT) {
            member.setMonthlyRecharge(member.getMonthlyRecharge().add(amount));
            checkAndUpgradeLevel(member);
        }

        member.setPlatformWalletBalance(newBalance);
        memberRepository.save(member);

        Transaction transaction = new Transaction();
        transaction.setMember(member);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setReferenceId(refId);

        transactionRepository.save(transaction);
    }

    /**
     * 扣除餘額（便捷方法）
     */
    @Transactional
    public void deductBalance(Long memberId, BigDecimal amount, String description) {
        updateWalletBalance(memberId, amount.negate(), Transaction.TransactionType.GACHA_SPEND, description);
    }

    /**
     * 增加餘額（便捷方法）
     */
    @Transactional
    public void addBalance(Long memberId, BigDecimal amount, String description) {
        updateWalletBalance(memberId, amount, Transaction.TransactionType.REFUND, description);
    }

    private void checkAndUpgradeLevel(Member member) {
        MemberLevel current = member.getLevel();

        // Fetch all enabled levels sorted by sort order
        List<MemberLevel> allLevels = memberLevelRepository.findByEnabledTrueOrderBySortOrderAsc();

        if (allLevels.isEmpty())
            return;

        // 新會員無等級時，或重新指派
        if (current == null) {
            member.setLevel(allLevels.get(0));
            current = allLevels.get(0);
        }

        MemberLevel targetLevel = current;

        // Find the highest eligible level using growthValue
        BigDecimal growthValueBD = BigDecimal.valueOf(member.getGrowthValue());
        for (MemberLevel level : allLevels) {
            if (level.getSortOrder() > current.getSortOrder() &&
                    growthValueBD.compareTo(level.getThreshold()) >= 0) {
                targetLevel = level;
            }
        }

        if (!targetLevel.equals(current)) {
            member.setLevel(targetLevel);
            member.setLastLevelReviewDate(LocalDate.now());
        }
    }
}
