package com.toy.store.repository;

import com.toy.store.model.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromoCodeRepository extends JpaRepository<PromoCode, Long> {

    Optional<PromoCode> findByCode(String code);

    List<PromoCode> findByCreatorMemberId(Long memberId);

    List<PromoCode> findByTypeAndEnabledTrue(PromoCode.CodeType type);

    boolean existsByCode(String code);
}
