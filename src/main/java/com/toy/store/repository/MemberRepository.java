package com.toy.store.repository;

import com.toy.store.model.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    @Query("SELECT m FROM Member m WHERE LOWER(m.username) LIKE :keyword OR LOWER(m.email) LIKE :keyword OR LOWER(m.nickname) LIKE :keyword")
    Page<Member> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
