package com.toy.store.service;

import com.toy.store.model.Member;
import com.toy.store.model.Transaction;
import com.toy.store.repository.MemberRepository;
import com.toy.store.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransactionService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private com.toy.store.repository.MemberLevelRepository memberLevelRepository;

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
                .orElseThrow(() -> new RuntimeException("Member not found"));

        BigDecimal newBalance = member.getPlatformWalletBalance().add(amount);

        // Check for sufficient funds if subtracting
        if (amount.compareTo(BigDecimal.ZERO) < 0 && newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Insufficient wallet balance");
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

    private void checkAndUpgradeLevel(Member member) {
        com.toy.store.model.MemberLevel current = member.getLevel();
        com.toy.store.model.MemberLevel next = current.next();

        // Check if eligible for next level(s)
        while (next != current && member.getMonthlyRecharge().compareTo(next.getThreshold()) >= 0) {
            current = next;
            next = current.next();
        }

        if (current != member.getLevel()) {
            member.setLevel(current);
        }
    }
}
