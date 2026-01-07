package com.toy.store.mapper;

import com.toy.store.model.PropCard;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 道具卡 MyBatis Mapper
 */
@Mapper
public interface PropCardMapper {

    @Select("SELECT * FROM prop_cards WHERE id = #{id}")
    Optional<PropCard> findById(Long id);

    @Select("SELECT * FROM prop_cards WHERE member_id = #{memberId} AND quantity > #{quantity}")
    List<PropCard> findByMemberIdAndQuantityGreaterThan(@Param("memberId") Long memberId,
            @Param("quantity") Integer quantity);

    @Select("SELECT * FROM prop_cards WHERE member_id = #{memberId} AND card_type = #{cardType}")
    Optional<PropCard> findByMemberIdAndCardType(@Param("memberId") Long memberId, @Param("cardType") String cardType);

    @Select("SELECT * FROM prop_cards WHERE member_id = #{memberId}")
    List<PropCard> findByMemberId(Long memberId);

    @Insert("INSERT INTO prop_cards (member_id, card_type, quantity, created_at, updated_at) " +
            "VALUES (#{memberId}, #{cardType}, #{quantity}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PropCard card);

    @Update("UPDATE prop_cards SET member_id = #{memberId}, card_type = #{cardType}, " +
            "quantity = #{quantity}, updated_at = #{updatedAt} WHERE id = #{id}")
    int update(PropCard card);

    @Delete("DELETE FROM prop_cards WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM prop_cards")
    List<PropCard> findAll();
}
