package com.toy.store.mapper;

import com.toy.store.model.LogisticsRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 物流紀錄 MyBatis Mapper
 */
@Mapper
public interface LogisticsRecordMapper {

    @Select("SELECT * FROM logistics_records WHERE id = #{id}")
    Optional<LogisticsRecord> findById(Long id);

    @Select("SELECT * FROM logistics_records WHERE tracking_no = #{trackingNo}")
    Optional<LogisticsRecord> findByTrackingNo(String trackingNo);

    @Select("SELECT * FROM logistics_records WHERE shipment_id = #{shipmentId}")
    Optional<LogisticsRecord> findByShipmentId(Long shipmentId);

    @Select("SELECT * FROM logistics_records WHERE status = #{status}")
    List<LogisticsRecord> findByStatus(@Param("status") String status);

    @Insert("INSERT INTO logistics_records (shipment_id, tracking_no, carrier, status, " +
            "shipped_at, delivered_at, created_at, updated_at) " +
            "VALUES (#{shipmentId}, #{trackingNo}, #{carrier}, #{status}, " +
            "#{shippedAt}, #{deliveredAt}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(LogisticsRecord record);

    @Update("UPDATE logistics_records SET shipment_id = #{shipmentId}, tracking_no = #{trackingNo}, " +
            "carrier = #{carrier}, status = #{status}, shipped_at = #{shippedAt}, " +
            "delivered_at = #{deliveredAt}, updated_at = #{updatedAt} WHERE id = #{id}")
    int update(LogisticsRecord record);

    @Delete("DELETE FROM logistics_records WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM logistics_records")
    List<LogisticsRecord> findAll();
}
