package com.nexuspay.domain.aggregate.payment;

import com.nexuspay.domain.event.PaymentFailedEvent;
import com.nexuspay.domain.event.PaymentSucceededEvent;
import com.nexuspay.domain.valueobject.Money;
import com.nexuspay.domain.valueobject.PaymentStatus;
import com.nexuspay.domain.valueobject.ProviderType;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class PaymentIntentAggregateTest {

    private PaymentIntentAggregate newAggregate(boolean manualCapture) {
        return new PaymentIntentAggregate(
                UUID.randomUUID(), UUID.randomUUID(), Money.of(1000, "USD"), "key123", manualCapture);
    }

    @Test
    void shouldCreatePaymentIntent() {
        var aggregate = newAggregate(false);
        assertEquals(PaymentStatus.REQUIRES_PAYMENT_METHOD, aggregate.getStatus());
    }

    @Test
    void shouldConfirmPayment() {
        var aggregate = newAggregate(false);
        aggregate.confirm("card", ProviderType.STRIPE, UUID.randomUUID());
        assertEquals(PaymentStatus.PROCESSING, aggregate.getStatus());
    }

    @Test
    void shouldRejectConfirmWhenNotInRequiresPaymentMethod() {
        var aggregate = newAggregate(false);
        aggregate.confirm("card", ProviderType.STRIPE, UUID.randomUUID());
        assertThrows(IllegalStateException.class,
                () -> aggregate.confirm("card", ProviderType.STRIPE, UUID.randomUUID()));
    }

    @Test
    void shouldMarkSucceeded() {
        var aggregate = newAggregate(false);
        aggregate.confirm("card", ProviderType.STRIPE, UUID.randomUUID());
        aggregate.markSucceeded("pi_123");
        assertEquals(PaymentStatus.SUCCEEDED, aggregate.getStatus());
        assertEquals("pi_123", aggregate.getProviderPaymentId());
    }

    @Test
    void shouldEmitSucceededEvent() {
        var aggregate = newAggregate(false);
        aggregate.confirm("card", ProviderType.STRIPE, UUID.randomUUID());
        aggregate.markSucceeded("pi_123");

        var events = aggregate.pullDomainEvents();
        assertEquals(1, events.size());
        assertInstanceOf(PaymentSucceededEvent.class, events.get(0));
    }

    @Test
    void shouldMarkFailed() {
        var aggregate = newAggregate(false);
        aggregate.confirm("card", ProviderType.STRIPE, UUID.randomUUID());
        aggregate.markFailed("card_declined", "Card declined", true);
        assertEquals(PaymentStatus.FAILED, aggregate.getStatus());
    }

    @Test
    void shouldEmitFailedEvent() {
        var aggregate = newAggregate(false);
        aggregate.confirm("card", ProviderType.STRIPE, UUID.randomUUID());
        aggregate.markFailed("card_declined", "Card declined", true);

        var events = aggregate.pullDomainEvents();
        assertEquals(1, events.size());
        assertInstanceOf(PaymentFailedEvent.class, events.get(0));
    }

    @Test
    void shouldCaptureManualPayment() {
        var aggregate = newAggregate(true);
        aggregate.confirm("card", ProviderType.STRIPE, UUID.randomUUID());
        aggregate.markSucceeded("pi_123");
        assertEquals(PaymentStatus.REQUIRES_CAPTURE, aggregate.getStatus());

        aggregate.capture();
        assertEquals(PaymentStatus.SUCCEEDED, aggregate.getStatus());
    }

    @Test
    void shouldRejectCaptureOnNonManualPayment() {
        var aggregate = newAggregate(false);
        aggregate.confirm("card", ProviderType.STRIPE, UUID.randomUUID());
        aggregate.markSucceeded("pi_123");
        assertEquals(PaymentStatus.SUCCEEDED, aggregate.getStatus());
        assertThrows(IllegalStateException.class, aggregate::capture);
    }

    @Test
    void shouldCancelPayment() {
        var aggregate = newAggregate(false);
        aggregate.cancel();
        assertEquals(PaymentStatus.CANCELED, aggregate.getStatus());
    }

    @Test
    void shouldRejectCancelOnSucceededPayment() {
        var aggregate = newAggregate(false);
        aggregate.confirm("card", ProviderType.STRIPE, UUID.randomUUID());
        aggregate.markSucceeded("pi_123");
        assertThrows(IllegalStateException.class, aggregate::cancel);
    }

    @Test
    void shouldReconstructWithoutTriggeringTransitions() {
        UUID id = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        var reconstructed = PaymentIntentAggregate.reconstruct(
                id, merchantId, Money.of(500, "EUR"), "key456", true,
                PaymentStatus.SUCCEEDED, ProviderType.SQUARE, UUID.randomUUID(),
                "sq_789", "card");

        assertEquals(id, reconstructed.getId());
        assertEquals(merchantId, reconstructed.getMerchantId());
        assertEquals(PaymentStatus.SUCCEEDED, reconstructed.getStatus());
        assertEquals(ProviderType.SQUARE, reconstructed.getResolvedProvider());
        assertEquals("sq_789", reconstructed.getProviderPaymentId());
        assertEquals("card", reconstructed.getPaymentMethodType());
        assertTrue(reconstructed.isManualCapture());
        // No events emitted during reconstruction
        assertTrue(reconstructed.pullDomainEvents().isEmpty());
    }

    @Test
    void shouldPullAndClearDomainEvents() {
        var aggregate = newAggregate(false);
        aggregate.confirm("card", ProviderType.STRIPE, UUID.randomUUID());
        aggregate.markSucceeded("pi_123");

        var firstPull = aggregate.pullDomainEvents();
        assertEquals(1, firstPull.size());

        var secondPull = aggregate.pullDomainEvents();
        assertTrue(secondPull.isEmpty());
    }
}
