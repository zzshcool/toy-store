package com.toy.store.mapper;

import com.toy.store.model.Activity;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 活動 MyBatis Mapper
 */
@Mapper
public interface ActivityMapper {

    @Select("SELECT * FROM activities WHERE id = #{id}")
    Optional<Activity> findById(Long id);

    @Select("SELECT * FROM activities WHERE status = #{status}")
    List<Activity> findByStatus(String status);

    @Select("SELECT * FROM activities")
    List<Activity> findAll();

    @Insert("INSERT INTO activities (name, description, start_date, end_date, status) " +
            "VALUES (#{name}, #{description}, #{startDate}, #{endDate}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Activity activity);

    @Update("UPDATE activities SET name = #{name}, description = #{description}, " +
            "start_date = #{startDate}, end_date = #{endDate}, status = #{status} WHERE id = #{id}")
    int update(Activity activity);

    @Delete("DELETE FROM activities WHERE id = #{id}")
    int deleteById(Long id);
}
