package com.toy.store.mapper;

import com.toy.store.model.SystemSetting;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 系統設定 MyBatis Mapper
 */
@Mapper
public interface SystemSettingMapper {

    @Select("SELECT * FROM system_settings WHERE id = #{id}")
    Optional<SystemSetting> findById(Long id);

    @Select("SELECT * FROM system_settings WHERE setting_key = #{key}")
    Optional<SystemSetting> findByKey(String key);

    @Select("SELECT * FROM system_settings")
    List<SystemSetting> findAll();

    @Insert("INSERT INTO system_settings (setting_key, setting_value, setting_type, description, updated_at) " +
            "VALUES (#{settingKey}, #{settingValue}, #{settingType}, #{description}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SystemSetting setting);

    @Update("UPDATE system_settings SET setting_value = #{settingValue}, setting_type = #{settingType}, " +
            "description = #{description}, updated_at = #{updatedAt} WHERE id = #{id}")
    int update(SystemSetting setting);

    @Update("UPDATE system_settings SET setting_value = #{value}, updated_at = NOW() WHERE setting_key = #{key}")
    int updateValueByKey(@Param("key") String key, @Param("value") String value);

    @Delete("DELETE FROM system_settings WHERE id = #{id}")
    int deleteById(Long id);
}
