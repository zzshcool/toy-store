package com.toy.store.repository;

import com.toy.store.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNo(String invoiceNo);

    Optional<Invoice> findByPaymentOrderId(Long paymentOrderId);

    List<Invoice> findByMemberIdOrderByCreatedAtDesc(Long memberId);
}
