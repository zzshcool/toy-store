package com.toy.store.mapper;

import com.toy.store.model.GachaIp;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * IP 主題 MyBatis Mapper
 */
@Mapper
public interface GachaIpMapper {

    @Select("SELECT * FROM gacha_ips WHERE id = #{id}")
    Optional<GachaIp> findById(Long id);

    @Select("SELECT * FROM gacha_ips")
    List<GachaIp> findAll();

    @Select("SELECT * FROM gacha_ips WHERE status = #{status}")
    List<GachaIp> findByStatus(String status);

    @Select("SELECT * FROM gacha_ips WHERE name = #{name}")
    Optional<GachaIp> findByName(String name);

    @Insert("INSERT INTO gacha_ips (name, description, image_url, status, created_at, updated_at) " +
            "VALUES (#{name}, #{description}, #{imageUrl}, #{status}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(GachaIp gachaIp);

    @Update("UPDATE gacha_ips SET name = #{name}, description = #{description}, " +
            "image_url = #{imageUrl}, status = #{status}, updated_at = #{updatedAt} WHERE id = #{id}")
    int update(GachaIp gachaIp);

    @Delete("DELETE FROM gacha_ips WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM gacha_ips LIMIT #{limit} OFFSET #{offset}")
    List<GachaIp> findAllPaged(@Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM gacha_ips")
    long count();
}
