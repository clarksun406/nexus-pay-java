package com.nexuspay.domain.repository;

import com.nexuspay.domain.aggregate.payment.PaymentIntentAggregate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentIntentRepository {
    PaymentIntentAggregate save(PaymentIntentAggregate aggregate);
    Optional<PaymentIntentAggregate> findById(UUID id);
    List<PaymentIntentAggregate> findByMerchantId(UUID merchantId);
    Optional<PaymentIntentAggregate> findByMerchantIdAndIdempotencyKey(UUID merchantId, String key);
}
