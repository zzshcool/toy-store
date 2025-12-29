package com.toy.store.repository;

import com.toy.store.model.MemberTagRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberTagRelationRepository extends JpaRepository<MemberTagRelation, Long> {

    List<MemberTagRelation> findByMemberId(Long memberId);

    List<MemberTagRelation> findByTagId(Long tagId);

    boolean existsByMemberIdAndTagId(Long memberId, Long tagId);

    void deleteByMemberIdAndTagId(Long memberId, Long tagId);
}
