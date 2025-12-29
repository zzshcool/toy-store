package com.toy.store.repository;

import com.toy.store.model.PromoCodeUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromoCodeUsageRepository extends JpaRepository<PromoCodeUsage, Long> {

    List<PromoCodeUsage> findByMemberId(Long memberId);

    long countByPromoCodeIdAndMemberId(Long codeId, Long memberId);

    long countByPromoCodeId(Long codeId);
}
