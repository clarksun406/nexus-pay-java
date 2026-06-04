package com.nexuspay.repository;

import com.nexuspay.domain.entity.PaymentIntent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, UUID> {
    List<PaymentIntent> findByMerchantId(UUID merchantId);
    List<PaymentIntent> findByStatus(PaymentIntent.PaymentStatus status);
    Optional<PaymentIntent> findByMerchantIdAndIdempotencyKey(UUID merchantId, String idempotencyKey);
    Optional<PaymentIntent> findByProviderPaymentId(String providerPaymentId);
}
