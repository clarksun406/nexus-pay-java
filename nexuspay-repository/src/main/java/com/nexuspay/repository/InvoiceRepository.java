package com.nexuspay.repository;

import com.nexuspay.domain.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    List<Invoice> findByMerchantId(UUID merchantId);

    List<Invoice> findByCustomerId(UUID customerId);

    List<Invoice> findBySubscriptionId(UUID subscriptionId);

    Optional<Invoice> findByPaymentIntentId(UUID paymentIntentId);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByMerchantIdAndStatus(UUID merchantId, Invoice.InvoiceStatus status);
}