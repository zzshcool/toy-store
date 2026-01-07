package com.toy.store.mapper;

import com.toy.store.model.Notification;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 通知 MyBatis Mapper
 */
@Mapper
public interface NotificationMapper {

    @Select("SELECT * FROM notifications WHERE id = #{id}")
    Optional<Notification> findById(Long id);

    @Select("SELECT * FROM notifications ORDER BY created_at DESC")
    List<Notification> findAllByOrderByCreatedAtDesc();

    @Insert("INSERT INTO notifications (title, content, type, target_url, created_at) " +
            "VALUES (#{title}, #{content}, #{type}, #{targetUrl}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Notification notification);

    @Update("UPDATE notifications SET title = #{title}, content = #{content}, " +
            "type = #{type}, target_url = #{targetUrl} WHERE id = #{id}")
    int update(Notification notification);

    @Delete("DELETE FROM notifications WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM notifications")
    List<Notification> findAll();
}
