package com.toy.store.repository;

import com.toy.store.model.MemberTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberTagRepository extends JpaRepository<MemberTag, Long> {

    Optional<MemberTag> findByName(String name);

    List<MemberTag> findByType(MemberTag.TagType type);
}
