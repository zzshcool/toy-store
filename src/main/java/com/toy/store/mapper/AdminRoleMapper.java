package com.toy.store.mapper;

import com.toy.store.model.AdminRole;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 管理員角色 MyBatis Mapper
 */
@Mapper
public interface AdminRoleMapper {

    @Select("SELECT * FROM admin_roles WHERE id = #{id}")
    Optional<AdminRole> findById(Long id);

    @Select("SELECT * FROM admin_roles WHERE name = #{name}")
    Optional<AdminRole> findByName(String name);

    @Select("SELECT * FROM admin_roles")
    List<AdminRole> findAll();

    @Insert("INSERT INTO admin_roles (name, description) VALUES (#{name}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AdminRole role);

    @Update("UPDATE admin_roles SET name = #{name}, description = #{description} WHERE id = #{id}")
    int update(AdminRole role);

    @Delete("DELETE FROM admin_roles WHERE id = #{id}")
    int deleteById(Long id);

    // 查詢角色的權限
    @Select("SELECT p.* FROM admin_permissions p " +
            "JOIN admin_role_permissions rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId}")
    List<com.toy.store.model.AdminPermission> findPermissionsByRoleId(Long roleId);

    // 新增角色權限關聯
    @Insert("INSERT INTO admin_role_permissions (role_id, permission_id) VALUES (#{roleId}, #{permissionId})")
    int addPermissionToRole(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    // 刪除角色所有權限關聯
    @Delete("DELETE FROM admin_role_permissions WHERE role_id = #{roleId}")
    int removeAllPermissionsFromRole(Long roleId);
}
