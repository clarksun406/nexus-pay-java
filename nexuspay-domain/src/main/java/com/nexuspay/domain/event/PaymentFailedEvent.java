package com.nexuspay.domain.event;

import java.time.Instant;
import java.util.UUID;

public record PaymentFailedEvent(
    UUID eventId,
    Instant occurredAt,
    UUID paymentIntentId,
    UUID merchantId,
    String failureCode,
    String failureMessage,
    boolean retryable
) implements DomainEvent {
    
    @Override
    public UUID getEventId() { return eventId; }
    
    @Override
    public Instant getOccurredAt() { return occurredAt; }
    
    @Override
    public UUID getAggregateId() { return paymentIntentId; }
}
