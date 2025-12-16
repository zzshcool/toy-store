package com.toy.store.repository;

import com.toy.store.model.GachaRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GachaRecordRepository extends JpaRepository<GachaRecord, Long> {

    List<GachaRecord> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    List<GachaRecord> findByMemberIdAndGachaType(Long memberId, GachaRecord.GachaType gachaType);

    List<GachaRecord> findTop50ByMemberIdOrderByCreatedAtDesc(Long memberId);

    // 查詢最近的抽獎紀錄（用於重複款判斷）
    List<GachaRecord> findByMemberIdAndPrizeName(Long memberId, String prizeName);

    // 全站最新抽獎紀錄（跑馬燈用）
    List<GachaRecord> findTop20ByOrderByCreatedAtDesc();
}
