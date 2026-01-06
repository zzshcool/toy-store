package com.toy.store.mapper;

import com.toy.store.model.AdminUser;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 管理員使用者 MyBatis Mapper
 */
@Mapper
public interface AdminUserMapper {

    @Select("SELECT * FROM admin_users WHERE id = #{id}")
    Optional<AdminUser> findById(Long id);

    @Select("SELECT * FROM admin_users WHERE username = #{username}")
    Optional<AdminUser> findByUsername(String username);

    @Select("SELECT * FROM admin_users")
    List<AdminUser> findAll();

    @Insert("INSERT INTO admin_users (username, password, email) VALUES (#{username}, #{password}, #{email})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AdminUser adminUser);

    @Update("UPDATE admin_users SET username = #{username}, password = #{password}, email = #{email} WHERE id = #{id}")
    int update(AdminUser adminUser);

    @Delete("DELETE FROM admin_users WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT COUNT(*) > 0 FROM admin_users WHERE username = #{username}")
    Boolean existsByUsername(String username);

    // 查詢管理員的角色
    @Select("SELECT r.* FROM admin_roles r " +
            "JOIN admin_user_roles ur ON r.id = ur.role_id " +
            "WHERE ur.admin_id = #{adminId}")
    List<com.toy.store.model.AdminRole> findRolesByAdminId(Long adminId);

    // 新增管理員角色關聯
    @Insert("INSERT INTO admin_user_roles (admin_id, role_id) VALUES (#{adminId}, #{roleId})")
    int addRoleToAdmin(@Param("adminId") Long adminId, @Param("roleId") Long roleId);

    // 刪除管理員所有角色關聯
    @Delete("DELETE FROM admin_user_roles WHERE admin_id = #{adminId}")
    int removeAllRolesFromAdmin(Long adminId);
}
