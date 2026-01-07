package com.toy.store.mapper;

import com.toy.store.model.AdminActionLog;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 管理員操作日誌 MyBatis Mapper
 */
@Mapper
public interface AdminActionLogMapper {

    @Select("SELECT * FROM admin_action_logs WHERE id = #{id}")
    Optional<AdminActionLog> findById(Long id);

    @Select("SELECT * FROM admin_action_logs WHERE admin_id = #{adminId} ORDER BY created_at DESC")
    List<AdminActionLog> findByAdminId(Long adminId);

    @Select("SELECT * FROM admin_action_logs ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<AdminActionLog> findAllPaged(@Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM admin_action_logs")
    long count();

    @Insert("INSERT INTO admin_action_logs (admin_id, admin_name, action, target_type, target_id, details, ip_address, created_at) "
            +
            "VALUES (#{adminId}, #{adminName}, #{action}, #{targetType}, #{targetId}, #{details}, #{ipAddress}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AdminActionLog log);

    @Select("SELECT * FROM admin_action_logs")
    List<AdminActionLog> findAll();
}
