package com.nexuspay.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.repository.PaymentIntentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProviderWebhookServiceTest {

    @Mock
    private PaymentIntentRepository paymentIntentRepository;

    @Mock
    private OutboxService outboxService;

    private ProviderWebhookService providerWebhookService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        providerWebhookService = new ProviderWebhookService(
                paymentIntentRepository, outboxService, new ObjectMapper());
    }

    // ---- Stripe webhook state transitions ----

    @Test
    void shouldSyncStripePaymentSucceededWebhook() {
        PaymentIntent intent = createIntent(PaymentIntent.PaymentStatus.PROCESSING);
        when(paymentIntentRepository.findByProviderPaymentId("pi_stripe_1"))
                .thenReturn(Optional.of(intent));

        String payload = """
                {
                    "type": "payment_intent.succeeded",
                    "data": {
                        "object": {
                            "id": "pi_stripe_1",
                            "status": "succeeded"
                        }
                    }
                }""";

        providerWebhookService.handleStripe(payload);

        assertEquals(PaymentIntent.PaymentStatus.SUCCEEDED, intent.getStatus());
        assertNotNull(intent.getProviderResponse());
        verify(paymentIntentRepository).save(intent);
        verify(outboxService).createPaymentEvent(intent, "payment_intent.succeeded");
    }

    @Test
    void shouldSyncStripePaymentFailedWebhook() {
        PaymentIntent intent = createIntent(PaymentIntent.PaymentStatus.PROCESSING);
        when(paymentIntentRepository.findByProviderPaymentId("pi_stripe_2"))
                .thenReturn(Optional.of(intent));

        String payload = """
                {
                    "type": "payment_intent.payment_failed",
                    "data": {
                        "object": {
                            "id": "pi_stripe_2",
                            "status": "requires_payment_method"
                        }
                    }
                }""";

        providerWebhookService.handleStripe(payload);

        assertEquals(PaymentIntent.PaymentStatus.FAILED, intent.getStatus());
        verify(paymentIntentRepository).save(intent);
        verify(outboxService).createPaymentEvent(intent, "payment_intent.failed");
    }

    @Test
    void shouldSyncStripePaymentCanceledWebhook() {
        PaymentIntent intent = createIntent(PaymentIntent.PaymentStatus.REQUIRES_CAPTURE);
        when(paymentIntentRepository.findByProviderPaymentId("pi_stripe_3"))
                .thenReturn(Optional.of(intent));

        String payload = """
                {
                    "type": "payment_intent.canceled",
                    "data": {
                        "object": {
                            "id": "pi_stripe_3",
                            "status": "canceled"
                        }
                    }
                }""";

        providerWebhookService.handleStripe(payload);

        assertEquals(PaymentIntent.PaymentStatus.CANCELED, intent.getStatus());
        verify(paymentIntentRepository).save(intent);
        verify(outboxService).createPaymentEvent(intent, "payment_intent.canceled");
    }

    @Test
    void shouldSkipOutboxWhenStripeStatusUnchanged() {
        PaymentIntent intent = createIntent(PaymentIntent.PaymentStatus.SUCCEEDED);
        when(paymentIntentRepository.findByProviderPaymentId("pi_stripe_4"))
                .thenReturn(Optional.of(intent));

        String payload = """
                {
                    "type": "payment_intent.succeeded",
                    "data": {
                        "object": {
                            "id": "pi_stripe_4",
                            "status": "succeeded"
                        }
                    }
                }""";

        providerWebhookService.handleStripe(payload);

        verify(paymentIntentRepository).save(intent);
        verify(outboxService, never()).createPaymentEvent(any(), anyString());
    }

    @Test
    void shouldIgnoreStripeWebhookWithoutDataObject() {
        String payload = """
                {
                    "type": "payment_intent.succeeded",
                    "data": {}
                }""";

        providerWebhookService.handleStripe(payload);

        verify(paymentIntentRepository, never()).findByProviderPaymentId(any());
    }

    // ---- Square webhook state transitions ----

    @Test
    void shouldSyncSquarePaymentCompletedWebhook() {
        PaymentIntent intent = createIntent(PaymentIntent.PaymentStatus.PROCESSING);
        when(paymentIntentRepository.findByProviderPaymentId("sq_pay_1"))
                .thenReturn(Optional.of(intent));

        String payload = """
                {
                    "data": {
                        "object": {
                            "payment": {
                                "id": "sq_pay_1",
                                "status": "COMPLETED"
                            }
                        }
                    }
                }""";

        providerWebhookService.handleSquare(payload);

        assertEquals(PaymentIntent.PaymentStatus.SUCCEEDED, intent.getStatus());
        verify(paymentIntentRepository).save(intent);
        verify(outboxService).createPaymentEvent(intent, "payment_intent.succeeded");
    }

    @Test
    void shouldSyncSquarePaymentFailedWebhook() {
        PaymentIntent intent = createIntent(PaymentIntent.PaymentStatus.PROCESSING);
        when(paymentIntentRepository.findByProviderPaymentId("sq_pay_2"))
                .thenReturn(Optional.of(intent));

        String payload = """
                {
                    "data": {
                        "object": {
                            "payment": {
                                "id": "sq_pay_2",
                                "status": "FAILED"
                            }
                        }
                    }
                }""";

        providerWebhookService.handleSquare(payload);

        assertEquals(PaymentIntent.PaymentStatus.FAILED, intent.getStatus());
        verify(paymentIntentRepository).save(intent);
        verify(outboxService).createPaymentEvent(intent, "payment_intent.failed");
    }

    @Test
    void shouldSyncSquareRefundWebhook() {
        PaymentIntent intent = createIntent(PaymentIntent.PaymentStatus.SUCCEEDED);
        when(paymentIntentRepository.findByProviderPaymentId("sq_pay_3"))
                .thenReturn(Optional.of(intent));

        String payload = """
                {
                    "data": {
                        "object": {
                            "refund": {
                                "id": "sq_pay_3",
                                "status": "COMPLETED"
                            }
                        }
                    }
                }""";

        providerWebhookService.handleSquare(payload);

        assertEquals(PaymentIntent.PaymentStatus.SUCCEEDED, intent.getStatus());
        verify(paymentIntentRepository).save(intent);
    }

    @Test
    void shouldIgnoreSquareWebhookWithoutPaymentOrRefundObject() {
        String payload = """
                {
                    "data": {
                        "object": {}
                    }
                }""";

        providerWebhookService.handleSquare(payload);

        verify(paymentIntentRepository, never()).findByProviderPaymentId(any());
    }

    // ---- status mapping edge cases ----

    @Test
    void shouldMapStripeRequiresCaptureStatus() {
        PaymentIntent intent = createIntent(PaymentIntent.PaymentStatus.REQUIRES_PAYMENT_METHOD);
        when(paymentIntentRepository.findByProviderPaymentId("pi_status_1"))
                .thenReturn(Optional.of(intent));

        String payload = """
                {
                    "type": "some.other.event",
                    "data": {
                        "object": {
                            "id": "pi_status_1",
                            "status": "requires_capture"
                        }
                    }
                }""";

        providerWebhookService.handleStripe(payload);

        assertEquals(PaymentIntent.PaymentStatus.REQUIRES_CAPTURE, intent.getStatus());
        verify(outboxService).createPaymentEvent(intent, "payment_intent.requires_capture");
    }

    @Test
    void shouldMapSquareApprovedStatus() {
        PaymentIntent intent = createIntent(PaymentIntent.PaymentStatus.PROCESSING);
        when(paymentIntentRepository.findByProviderPaymentId("sq_status_1"))
                .thenReturn(Optional.of(intent));

        String payload = """
                {
                    "data": {
                        "object": {
                            "payment": {
                                "id": "sq_status_1",
                                "status": "APPROVED"
                            }
                        }
                    }
                }""";

        providerWebhookService.handleSquare(payload);

        assertEquals(PaymentIntent.PaymentStatus.REQUIRES_CAPTURE, intent.getStatus());
    }

    @Test
    void shouldNotCrashOnMalformedWebhookPayload() {
        providerWebhookService.handleStripe("not valid json");
        providerWebhookService.handleSquare("{broken");

        verify(paymentIntentRepository, never()).findByProviderPaymentId(any());
    }

    @Test
    void shouldNotSaveWhenNoLocalIntentFound() {
        when(paymentIntentRepository.findByProviderPaymentId("pi_unknown"))
                .thenReturn(Optional.empty());

        String payload = """
                {
                    "type": "payment_intent.succeeded",
                    "data": {
                        "object": {
                            "id": "pi_unknown",
                            "status": "succeeded"
                        }
                    }
                }""";

        providerWebhookService.handleStripe(payload);

        verify(paymentIntentRepository, never()).save(any());
    }

    private PaymentIntent createIntent(PaymentIntent.PaymentStatus status) {
        PaymentIntent intent = new PaymentIntent();
        intent.setId(UUID.randomUUID());
        intent.setMerchantId(UUID.randomUUID());
        intent.setStatus(status);
        intent.setAmount(java.math.BigInteger.valueOf(1000));
        intent.setCurrency("usd");
        intent.setProviderPaymentId("pi_test");
        return intent;
    }
}