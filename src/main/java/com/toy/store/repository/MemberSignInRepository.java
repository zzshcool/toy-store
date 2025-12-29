package com.toy.store.repository;

import com.toy.store.model.MemberSignIn;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface MemberSignInRepository extends JpaRepository<MemberSignIn, Long> {
    Optional<MemberSignIn> findByMemberIdAndSignInDate(Long memberId, LocalDate signInDate);

    List<MemberSignIn> findByMemberIdOrderBySignInDateDesc(Long memberId);
}
