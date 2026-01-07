package com.toy.store.mapper;

import com.toy.store.model.Transaction;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 交易紀錄 MyBatis Mapper
 */
@Mapper
public interface TransactionMapper {

        @Select("SELECT * FROM transactions WHERE id = #{id}")
        Optional<Transaction> findById(Long id);

        @Select("SELECT * FROM transactions WHERE member_id = #{memberId} ORDER BY created_at DESC")
        List<Transaction> findByMemberId(Long memberId);

        @Select("SELECT * FROM transactions WHERE member_id = #{memberId} ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
        List<Transaction> findByMemberIdPaged(@Param("memberId") Long memberId,
                        @Param("offset") int offset,
                        @Param("limit") int limit);

        @Select("SELECT * FROM transactions WHERE member_id = #{memberId} AND type = #{type} ORDER BY created_at DESC")
        List<Transaction> findByMemberIdAndType(@Param("memberId") Long memberId, @Param("type") String type);

        @Select("SELECT * FROM transactions WHERE created_at >= #{startTime} AND created_at <= #{endTime} ORDER BY created_at DESC")
        List<Transaction> findByCreatedAtBetween(@Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        @Select("SELECT * FROM transactions ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
        List<Transaction> findAllPaged(@Param("offset") int offset, @Param("limit") int limit);

        @Insert("INSERT INTO transactions (member_id, type, amount, balance_after, description, " +
                        "reference_id, reference_type, created_at) " +
                        "VALUES (#{memberId}, #{type}, #{amount}, #{balanceAfter}, #{description}, " +
                        "#{referenceId}, #{referenceType}, #{createdAt})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insert(Transaction transaction);

        @Select("SELECT COUNT(*) FROM transactions WHERE member_id = #{memberId}")
        long countByMemberId(Long memberId);

        @Select("SELECT COUNT(*) FROM transactions")
        long count();

        @Select("SELECT * FROM transactions")
        List<Transaction> findAll();

        @Select("SELECT SUM(amount) FROM transactions WHERE amount > 0 AND created_at >= #{startTime} AND created_at < #{endTime}")
        BigDecimal sumPositiveAmountBetween(@Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        @Select("SELECT SUM(amount) FROM transactions WHERE amount > 0 AND created_at >= #{startTime}")
        BigDecimal sumPositiveAmountSince(@Param("startTime") LocalDateTime startTime);

        @Select("SELECT * FROM transactions ORDER BY created_at DESC LIMIT 10")
        List<Transaction> findTop10ByOrderByCreatedAtDesc();

        // 兼容別名方法
        @Select("SELECT * FROM transactions WHERE member_id = #{memberId} ORDER BY created_at DESC")
        List<Transaction> findByMemberIdOrderByTimestampDesc(Long memberId);
}
