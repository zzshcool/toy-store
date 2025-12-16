package com.toy.store.repository;

import com.toy.store.model.ShardTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ShardTransactionRepository extends JpaRepository<ShardTransaction, Long> {

    List<ShardTransaction> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    List<ShardTransaction> findByMemberIdAndType(Long memberId, ShardTransaction.TransactionType type);

    // 取得最近N筆交易
    List<ShardTransaction> findTop20ByMemberIdOrderByCreatedAtDesc(Long memberId);

    // 計算總獲得碎片
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM ShardTransaction t WHERE t.memberId = :memberId AND t.amount > 0")
    int sumEarnedShards(@Param("memberId") Long memberId);
}
