package com.toy.store.mapper;

import com.toy.store.model.PaymentOrder;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 支付訂單 MyBatis Mapper
 */
@Mapper
public interface PaymentOrderMapper {

    @Select("SELECT * FROM payment_orders WHERE id = #{id}")
    Optional<PaymentOrder> findById(Long id);

    @Select("SELECT * FROM payment_orders WHERE order_no = #{orderNo}")
    Optional<PaymentOrder> findByOrderNo(String orderNo);

    @Select("SELECT * FROM payment_orders WHERE member_id = #{memberId} ORDER BY created_at DESC")
    List<PaymentOrder> findByMemberId(Long memberId);

    @Select("SELECT * FROM payment_orders WHERE status = #{status} ORDER BY created_at DESC")
    List<PaymentOrder> findByStatus(String status);

    @Select("SELECT * FROM payment_orders ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<PaymentOrder> findAllPaged(@Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM payment_orders")
    long count();

    @Insert("INSERT INTO payment_orders (member_id, order_no, amount, payment_method, status, paid_at, created_at) " +
            "VALUES (#{memberId}, #{orderNo}, #{amount}, #{paymentMethod}, #{status}, #{paidAt}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PaymentOrder order);

    @Update("UPDATE payment_orders SET status = #{status}, paid_at = #{paidAt} WHERE id = #{id}")
    int update(PaymentOrder order);

    @Delete("DELETE FROM payment_orders WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM payment_orders")
    List<PaymentOrder> findAll();
}
