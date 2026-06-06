package com.nexuspay.repository.impl;

import com.nexuspay.domain.aggregate.payment.PaymentIntentAggregate;
import com.nexuspay.domain.repository.PaymentIntentRepository;
import com.nexuspay.domain.valueobject.Money;
import com.nexuspay.domain.valueobject.PaymentStatus;
import com.nexuspay.domain.valueobject.ProviderType;
import com.nexuspay.repository.PaymentIntentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentIntentRepositoryImpl implements PaymentIntentRepository {
    
    private final PaymentIntentJpaRepository jpaRepository;
    
    @Override
    public PaymentIntentAggregate save(PaymentIntentAggregate aggregate) {
        var entity = toEntity(aggregate);
        jpaRepository.save(entity);
        return aggregate;
    }
    
    @Override
    public Optional<PaymentIntentAggregate> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toAggregate);
    }
    
    @Override
    public List<PaymentIntentAggregate> findByMerchantId(UUID merchantId) {
        return jpaRepository.findByMerchantId(merchantId).stream().map(this::toAggregate).toList();
    }
    
    @Override
    public Optional<PaymentIntentAggregate> findByMerchantIdAndIdempotencyKey(UUID merchantId, String key) {
        return jpaRepository.findByMerchantIdAndIdempotencyKey(merchantId, key).map(this::toAggregate);
    }
    
    private com.nexuspay.domain.entity.PaymentIntent toEntity(PaymentIntentAggregate agg) {
        var entity = new com.nexuspay.domain.entity.PaymentIntent();
        entity.setId(agg.getId());
        entity.setMerchantId(agg.getMerchantId());
        entity.setAmount(agg.getAmount().amount());
        entity.setCurrency(agg.getAmount().currency().getCurrencyCode());
        entity.setStatus(com.nexuspay.domain.entity.PaymentIntent.PaymentStatus.valueOf(agg.getStatus().name()));
        entity.setIdempotencyKey(agg.getIdempotencyKey());
        entity.setCaptureMethod(agg.isManualCapture() 
                ? com.nexuspay.domain.entity.PaymentIntent.CaptureMethod.MANUAL 
                : com.nexuspay.domain.entity.PaymentIntent.CaptureMethod.AUTOMATIC);
        if (agg.getResolvedProvider() != null) {
            entity.setResolvedProvider(com.nexuspay.domain.entity.PaymentIntent.Provider.valueOf(agg.getResolvedProvider().name()));
        }
        entity.setConnectorAccountId(agg.getConnectorAccountId());
        entity.setProviderPaymentId(agg.getProviderPaymentId());
        entity.setPaymentMethodType(agg.getPaymentMethodType());
        return entity;
    }
    
    private PaymentIntentAggregate toAggregate(com.nexuspay.domain.entity.PaymentIntent e) {
        return PaymentIntentAggregate.reconstruct(
                e.getId(), e.getMerchantId(),
                Money.of(e.getAmount(), e.getCurrency()),
                e.getIdempotencyKey(),
                e.getCaptureMethod() == com.nexuspay.domain.entity.PaymentIntent.CaptureMethod.MANUAL,
                PaymentStatus.valueOf(e.getStatus().name()),
                e.getResolvedProvider() != null ? ProviderType.valueOf(e.getResolvedProvider().name()) : null,
                e.getConnectorAccountId(),
                e.getProviderPaymentId(),
                e.getPaymentMethodType());
    }
}
