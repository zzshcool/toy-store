package com.toy.store.mapper;

import com.toy.store.model.AdminPermission;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 管理員權限 MyBatis Mapper
 */
@Mapper
public interface AdminPermissionMapper {

    @Select("SELECT * FROM admin_permissions WHERE id = #{id}")
    Optional<AdminPermission> findById(Long id);

    @Select("SELECT * FROM admin_permissions WHERE code = #{code}")
    Optional<AdminPermission> findByCode(String code);

    @Select("SELECT * FROM admin_permissions")
    List<AdminPermission> findAll();

    @Insert("INSERT INTO admin_permissions (code, name, description) VALUES (#{code}, #{name}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AdminPermission permission);

    @Update("UPDATE admin_permissions SET code = #{code}, name = #{name}, description = #{description} WHERE id = #{id}")
    int update(AdminPermission permission);

    @Delete("DELETE FROM admin_permissions WHERE id = #{id}")
    int deleteById(Long id);
}
