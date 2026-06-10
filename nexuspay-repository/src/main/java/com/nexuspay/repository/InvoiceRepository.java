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

    Optional<Invoice> findByMerchantIdAndId(UUID merchantId, UUID id);

    List<Invoice> findByCustomerId(UUID customerId);

    List<Invoice> findByMerchantIdAndCustomerId(UUID merchantId, UUID customerId);

    List<Invoice> findBySubscriptionId(UUID subscriptionId);

    List<Invoice> findByMerchantIdAndSubscriptionId(UUID merchantId, UUID subscriptionId);

    Optional<Invoice> findByPaymentIntentId(UUID paymentIntentId);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByMerchantIdAndStatus(UUID merchantId, Invoice.InvoiceStatus status);
}
