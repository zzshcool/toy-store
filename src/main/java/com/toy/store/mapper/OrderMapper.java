package com.toy.store.mapper;

import com.toy.store.model.Order;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 訂單 MyBatis Mapper
 */
@Mapper
public interface OrderMapper {

    @Select("SELECT * FROM orders WHERE id = #{id}")
    Optional<Order> findById(Long id);

    @Select("SELECT * FROM orders")
    List<Order> findAll();

    @Select("SELECT * FROM orders WHERE member_id = #{memberId} ORDER BY create_time DESC")
    List<Order> findByMemberId(Long memberId);

    @Select("SELECT * FROM orders WHERE status = #{status} ORDER BY create_time DESC")
    List<Order> findByStatus(String status);

    @Insert("INSERT INTO orders (member_id, total_price, discount_amount, coupon_name, status, create_time) " +
            "VALUES (#{member.id}, #{totalPrice}, #{discountAmount}, #{couponName}, #{status}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    @Update("UPDATE orders SET total_price = #{totalPrice}, discount_amount = #{discountAmount}, " +
            "coupon_name = #{couponName}, status = #{status} WHERE id = #{id}")
    int update(Order order);

    @Delete("DELETE FROM orders WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM orders LIMIT #{limit} OFFSET #{offset}")
    List<Order> findAllPaged(@Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM orders")
    long count();
}
