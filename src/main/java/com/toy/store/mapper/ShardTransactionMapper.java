package com.toy.store.mapper;

import com.toy.store.model.ShardTransaction;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 碎片交易 MyBatis Mapper
 */
@Mapper
public interface ShardTransactionMapper {

    @Select("SELECT * FROM shard_transactions WHERE id = #{id}")
    Optional<ShardTransaction> findById(Long id);

    @Select("SELECT * FROM shard_transactions WHERE member_id = #{memberId} ORDER BY created_at DESC")
    List<ShardTransaction> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    @Select("SELECT * FROM shard_transactions WHERE member_id = #{memberId} AND type = #{type}")
    List<ShardTransaction> findByMemberIdAndType(@Param("memberId") Long memberId, @Param("type") String type);

    @Select("SELECT * FROM shard_transactions WHERE member_id = #{memberId} ORDER BY created_at DESC LIMIT 20")
    List<ShardTransaction> findTop20ByMemberIdOrderByCreatedAtDesc(Long memberId);

    @Select("SELECT COALESCE(SUM(amount), 0) FROM shard_transactions WHERE member_id = #{memberId} AND amount > 0")
    int sumEarnedShards(@Param("memberId") Long memberId);

    @Insert("INSERT INTO shard_transactions (member_id, type, amount, description, created_at) " +
            "VALUES (#{memberId}, #{type}, #{amount}, #{description}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ShardTransaction transaction);

    @Update("UPDATE shard_transactions SET member_id = #{memberId}, type = #{type}, " +
            "amount = #{amount}, description = #{description} WHERE id = #{id}")
    int update(ShardTransaction transaction);

    @Delete("DELETE FROM shard_transactions WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM shard_transactions")
    List<ShardTransaction> findAll();
}
