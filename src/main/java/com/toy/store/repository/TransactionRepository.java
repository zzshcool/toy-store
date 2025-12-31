package com.toy.store.repository;

import com.toy.store.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByMemberIdOrderByTimestampDesc(Long memberId);

    // Dashboard 統計查詢
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.amount > 0 AND t.timestamp BETWEEN :start AND :end")
    BigDecimal sumPositiveAmountBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<Transaction> findTop10ByOrderByTimestampDesc();

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.amount > 0 AND t.timestamp >= :start")
    BigDecimal sumPositiveAmountSince(@Param("start") LocalDateTime start);
}
