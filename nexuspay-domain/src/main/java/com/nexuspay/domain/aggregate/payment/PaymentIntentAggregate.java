package com.nexuspay.domain.aggregate.payment;

import com.nexuspay.domain.event.*;
import com.nexuspay.domain.valueobject.*;
import lombok.Getter;

import java.time.Instant;
import java.util.*;

@Getter
public class PaymentIntentAggregate {
    
    private final UUID id;
    private final UUID merchantId;
    private final Money amount;
    private PaymentStatus status;
    private final String idempotencyKey;
    private final boolean manualCapture;
    private ProviderType resolvedProvider;
    private UUID connectorAccountId;
    private String providerPaymentId;
    private String paymentMethodType;
    private final List<DomainEvent> domainEvents = new ArrayList<>();
    
    public PaymentIntentAggregate(UUID id, UUID merchantId, Money amount, String idempotencyKey, boolean manualCapture) {
        this.id = id;
        this.merchantId = merchantId;
        this.amount = amount;
        this.idempotencyKey = idempotencyKey;
        this.manualCapture = manualCapture;
        this.status = PaymentStatus.REQUIRES_PAYMENT_METHOD;
    }

    /**
     * Reconstruct an aggregate from persisted state without triggering
     * state transitions or emitting domain events.
     */
    public static PaymentIntentAggregate reconstruct(UUID id, UUID merchantId, Money amount,
                                                      String idempotencyKey, boolean manualCapture,
                                                      PaymentStatus status, ProviderType resolvedProvider,
                                                      UUID connectorAccountId, String providerPaymentId,
                                                      String paymentMethodType) {
        PaymentIntentAggregate agg = new PaymentIntentAggregate(id, merchantId, amount, idempotencyKey, manualCapture);
        agg.status = status;
        agg.resolvedProvider = resolvedProvider;
        agg.connectorAccountId = connectorAccountId;
        agg.providerPaymentId = providerPaymentId;
        agg.paymentMethodType = paymentMethodType;
        return agg;
    }
    
    public void confirm(String paymentMethodType, ProviderType provider, UUID connectorAccountId) {
        if (!status.canConfirm()) {
            throw new IllegalStateException("Cannot confirm in status: " + status);
        }
        this.paymentMethodType = paymentMethodType;
        this.resolvedProvider = provider;
        this.connectorAccountId = connectorAccountId;
        this.status = PaymentStatus.PROCESSING;
    }
    
    public void markSucceeded(String providerPaymentId) {
        this.providerPaymentId = providerPaymentId;
        this.status = manualCapture ? PaymentStatus.REQUIRES_CAPTURE : PaymentStatus.SUCCEEDED;
        emit(new PaymentSucceededEvent(UUID.randomUUID(), Instant.now(), id, merchantId, resolvedProvider.name(), providerPaymentId));
    }
    
    public void markFailed(String failureCode, String failureMessage, boolean retryable) {
        this.status = PaymentStatus.FAILED;
        emit(new PaymentFailedEvent(UUID.randomUUID(), Instant.now(), id, merchantId, failureCode, failureMessage, retryable));
    }
    
    public void capture() {
        if (!status.canCapture()) throw new IllegalStateException("Cannot capture");
        this.status = PaymentStatus.SUCCEEDED;
    }
    
    public void cancel() {
        if (!status.canCancel()) throw new IllegalStateException("Cannot cancel");
        this.status = PaymentStatus.CANCELED;
    }
    
    private void emit(DomainEvent event) {
        domainEvents.add(event);
        EventPublisher.publish(event);
    }
    
    public List<DomainEvent> pullDomainEvents() {
        var events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }
}
