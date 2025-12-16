package com.toy.store.repository;

import com.toy.store.model.MemberLuckyValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MemberLuckyValueRepository extends JpaRepository<MemberLuckyValue, Long> {

    Optional<MemberLuckyValue> findByMemberId(Long memberId);

    boolean existsByMemberId(Long memberId);
}
