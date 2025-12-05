package com.toy.store.repository;

import com.toy.store.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByMemberIdOrderByTimestampDesc(Long memberId);
}
