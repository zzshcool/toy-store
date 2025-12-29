package com.toy.store.repository;

import com.toy.store.model.PropCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropCardRepository extends JpaRepository<PropCard, Long> {

    List<PropCard> findByMemberIdAndQuantityGreaterThan(Long memberId, Integer quantity);

    Optional<PropCard> findByMemberIdAndCardType(Long memberId, PropCard.CardType cardType);

    List<PropCard> findByMemberId(Long memberId);
}
