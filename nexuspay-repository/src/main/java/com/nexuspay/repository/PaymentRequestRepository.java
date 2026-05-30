package com.nexuspay.repository;

import com.nexuspay.domain.entity.PaymentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRequestRepository extends JpaRepository<PaymentRequest, UUID> {
    List<PaymentRequest> findByPaymentIntentId(UUID paymentIntentId);
    List<PaymentRequest> findByPaymentIntentIdOrderByCreatedAtDesc(UUID paymentIntentId);
}
