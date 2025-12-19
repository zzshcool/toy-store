package com.toy.store.repository;

import com.toy.store.model.MemberActionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberActionLogRepository extends JpaRepository<MemberActionLog, Long> {
    List<MemberActionLog> findByMemberIdOrderByTimestampDesc(Long memberId);
}
