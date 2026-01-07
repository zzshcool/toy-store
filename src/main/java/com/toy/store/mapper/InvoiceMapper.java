package com.toy.store.mapper;

import com.toy.store.model.Invoice;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 發票 MyBatis Mapper
 */
@Mapper
public interface InvoiceMapper {

        @Select("SELECT * FROM invoices WHERE id = #{id}")
        Optional<Invoice> findById(Long id);

        @Select("SELECT * FROM invoices WHERE invoice_no = #{invoiceNo}")
        Optional<Invoice> findByInvoiceNo(String invoiceNo);

        @Select("SELECT * FROM invoices WHERE payment_order_id = #{paymentOrderId}")
        Optional<Invoice> findByPaymentOrderId(Long paymentOrderId);

        @Select("SELECT * FROM invoices WHERE member_id = #{memberId} ORDER BY created_at DESC")
        List<Invoice> findByMemberIdOrderByCreatedAtDesc(Long memberId);

        @Select("SELECT * FROM invoices ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
        List<Invoice> findAllPaged(@Param("offset") int offset, @Param("limit") int limit);

        @Insert("INSERT INTO invoices (invoice_no, payment_order_id, member_id, invoice_type, " +
                        "carrier_type, carrier_no, buyer_name, buyer_tax_id, amount, status, created_at) " +
                        "VALUES (#{invoiceNo}, #{paymentOrderId}, #{memberId}, #{invoiceType}, " +
                        "#{carrierType}, #{carrierNo}, #{buyerName}, #{buyerTaxId}, #{amount}, #{status}, #{createdAt})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insert(Invoice invoice);

        @Update("UPDATE invoices SET invoice_no = #{invoiceNo}, payment_order_id = #{paymentOrderId}, " +
                        "member_id = #{memberId}, invoice_type = #{invoiceType}, carrier_type = #{carrierType}, " +
                        "carrier_no = #{carrierNo}, buyer_name = #{buyerName}, buyer_tax_id = #{buyerTaxId}, " +
                        "amount = #{amount}, status = #{status} WHERE id = #{id}")
        int update(Invoice invoice);

        @Delete("DELETE FROM invoices WHERE id = #{id}")
        int deleteById(Long id);

        @Select("SELECT * FROM invoices")
        List<Invoice> findAll();
}
