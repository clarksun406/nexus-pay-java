package com.nexuspay.domain.aggregate.payment;

import com.nexuspay.domain.valueobject.Money;
import com.nexuspay.domain.valueobject.PaymentStatus;
import com.nexuspay.domain.valueobject.ProviderType;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class PaymentIntentAggregateTest {
    
    @Test
    void shouldCreatePaymentIntent() {
        var aggregate = new PaymentIntentAggregate(
            UUID.randomUUID(), UUID.randomUUID(), Money.of(1000, "USD"), "key123", false);
        
        assertEquals(PaymentStatus.REQUIRES_PAYMENT_METHOD, aggregate.getStatus());
    }
    
    @Test
    void shouldConfirmPayment() {
        var aggregate = new PaymentIntentAggregate(
            UUID.randomUUID(), UUID.randomUUID(), Money.of(1000, "USD"), "key123", false);
        
        aggregate.confirm("card", ProviderType.STRIPE, UUID.randomUUID());
        
        assertEquals(PaymentStatus.PROCESSING, aggregate.getStatus());
    }
    
    @Test
    void shouldMarkSucceeded() {
        var aggregate = new PaymentIntentAggregate(
            UUID.randomUUID(), UUID.randomUUID(), Money.of(1000, "USD"), "key123", false);
        aggregate.confirm("card", ProviderType.STRIPE, UUID.randomUUID());
        
        aggregate.markSucceeded("pi_123");
        
        assertEquals(PaymentStatus.SUCCEEDED, aggregate.getStatus());
        assertEquals("pi_123", aggregate.getProviderPaymentId());
    }
    
    @Test
    void shouldMarkFailed() {
        var aggregate = new PaymentIntentAggregate(
            UUID.randomUUID(), UUID.randomUUID(), Money.of(1000, "USD"), "key123", false);
        aggregate.confirm("card", ProviderType.STRIPE, UUID.randomUUID());
        
        aggregate.markFailed("card_declined", "Card declined", true);
        
        assertEquals(PaymentStatus.FAILED, aggregate.getStatus());
    }
    
    @Test
    void shouldCaptureManualPayment() {
        var aggregate = new PaymentIntentAggregate(
            UUID.randomUUID(), UUID.randomUUID(), Money.of(1000, "USD"), "key123", true);
        aggregate.confirm("card", ProviderType.STRIPE, UUID.randomUUID());
        aggregate.markSucceeded("pi_123");
        
        assertEquals(PaymentStatus.REQUIRES_CAPTURE, aggregate.getStatus());
        
        aggregate.capture();
        
        assertEquals(PaymentStatus.SUCCEEDED, aggregate.getStatus());
    }
}
