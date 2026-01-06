package com.toy.store.mapper;

import com.toy.store.model.Cart;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 購物車 MyBatis Mapper
 */
@Mapper
public interface CartMapper {

    @Select("SELECT * FROM carts WHERE id = #{id}")
    Optional<Cart> findById(Long id);

    @Select("SELECT * FROM carts WHERE member_id = #{memberId}")
    Optional<Cart> findByMemberId(Long memberId);

    @Insert("INSERT INTO carts (member_id, created_at, updated_at) VALUES (#{memberId}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Cart cart);

    @Update("UPDATE carts SET updated_at = #{updatedAt} WHERE id = #{id}")
    int update(Cart cart);

    @Delete("DELETE FROM carts WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM carts")
    List<Cart> findAll();
}
