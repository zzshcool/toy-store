package com.toy.store.repository;

import com.toy.store.model.MemberMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberMessageRepository extends JpaRepository<MemberMessage, Long> {

    List<MemberMessage> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    List<MemberMessage> findByMemberIdAndReadFalseOrderByCreatedAtDesc(Long memberId);

    long countByMemberIdAndReadFalse(Long memberId);
}
