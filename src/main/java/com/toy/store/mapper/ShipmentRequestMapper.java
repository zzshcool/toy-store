package com.toy.store.mapper;

import com.toy.store.model.ShipmentRequest;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 出貨申請 MyBatis Mapper
 */
@Mapper
public interface ShipmentRequestMapper {

        @Select("SELECT * FROM shipment_requests WHERE id = #{id}")
        Optional<ShipmentRequest> findById(Long id);

        @Select("SELECT * FROM shipment_requests WHERE request_no = #{requestNo}")
        Optional<ShipmentRequest> findByRequestNo(String requestNo);

        @Select("SELECT * FROM shipment_requests WHERE member_id = #{memberId} ORDER BY created_at DESC")
        List<ShipmentRequest> findByMemberId(Long memberId);

        @Select("SELECT * FROM shipment_requests WHERE status = #{status} ORDER BY created_at DESC")
        List<ShipmentRequest> findByStatus(String status);

        @Insert("INSERT INTO shipment_requests (member_id, request_no, item_count, shipping_fee, " +
                        "recipient_name, recipient_phone, recipient_address, status, created_at) " +
                        "VALUES (#{memberId}, #{requestNo}, #{itemCount}, #{shippingFee}, " +
                        "#{recipientName}, #{recipientPhone}, #{recipientAddress}, #{status}, #{createdAt})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insert(ShipmentRequest request);

        @Update("UPDATE shipment_requests SET status = #{status}, item_count = #{itemCount}, " +
                        "shipping_fee = #{shippingFee} WHERE id = #{id}")
        int update(ShipmentRequest request);

        @Delete("DELETE FROM shipment_requests WHERE id = #{id}")
        int deleteById(Long id);

        @Select("SELECT * FROM shipment_requests")
        List<ShipmentRequest> findAll();

        @Select({
                        "<script>",
                        "SELECT COUNT(*) FROM shipment_requests WHERE status IN",
                        "<foreach item='item' collection='statuses' open='(' separator=',' close=')'>",
                        "#{item}",
                        "</foreach>",
                        "</script>"
        })
        long countByStatusIn(@Param("statuses") List<String> statuses);

        @Select("SELECT * FROM shipment_requests WHERE member_id = #{memberId} ORDER BY created_at DESC")
        List<ShipmentRequest> findByMemberIdOrderByCreatedAtDesc(Long memberId);

        @Select("SELECT * FROM shipment_requests WHERE status = #{status} ORDER BY created_at ASC")
        List<ShipmentRequest> findByStatusOrderByCreatedAtAsc(String status);
}
