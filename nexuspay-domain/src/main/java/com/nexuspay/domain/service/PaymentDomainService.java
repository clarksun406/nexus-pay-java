package com.nexuspay.domain.service;

import com.nexuspay.domain.aggregate.payment.PaymentIntentAggregate;
import com.nexuspay.domain.event.DomainEvent;
import com.nexuspay.domain.valueobject.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentDomainService {
    
    public PaymentIntentAggregate createPaymentIntent(
            UUID merchantId, Money amount, String idempotencyKey, boolean manualCapture) {
        return new PaymentIntentAggregate(
                UUID.randomUUID(), merchantId, amount, idempotencyKey, manualCapture);
    }
    
    public void confirmPayment(PaymentIntentAggregate aggregate, 
                               String paymentMethodType,
                               ProviderType provider, 
                               UUID connectorAccountId) {
        aggregate.confirm(paymentMethodType, provider, connectorAccountId);
    }
    
    public void markPaymentSucceeded(PaymentIntentAggregate aggregate, String providerPaymentId) {
        aggregate.markSucceeded(providerPaymentId);
    }
    
    public void markPaymentFailed(PaymentIntentAggregate aggregate, 
                                  String failureCode, String failureMessage, boolean retryable) {
        aggregate.markFailed(failureCode, failureMessage, retryable);
    }
}
