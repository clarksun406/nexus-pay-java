package com.nexuspay.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexuspay.domain.entity.OutboxEvent;
import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigInteger;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OutboxServiceTest {

    @Mock private OutboxEventRepository outboxEventRepo;
    @Mock private WebhookDeliveryService webhookDeliveryService;
    @InjectMocks private OutboxService outboxService;

    @BeforeEach
    void setUp() { MockitoAnnotations.openMocks(this); }

    @Test
    void shouldCreatePaymentEvent() {
        UUID merchantId = UUID.randomUUID();
        PaymentIntent intent = new PaymentIntent();
        intent.setId(UUID.randomUUID());
        intent.setMerchantId(merchantId);
        intent.setStatus(PaymentIntent.PaymentStatus.SUCCEEDED);
        intent.setAmount(BigInteger.valueOf(1000));
        intent.setCurrency("usd");

        when(outboxEventRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OutboxEvent event = outboxService.createPaymentEvent(intent, "payment_intent.succeeded");

        assertNotNull(event);
        assertTrue(event.getPayload().contains(merchantId.toString()));
    }

    @Test
    void shouldCreateGenericEvent() {
        when(outboxEventRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OutboxEvent event = outboxService.createEvent("subscription", UUID.randomUUID(),
                "subscription.renewed", "{\"test\":true}");

        assertNotNull(event);
        assertEquals("subscription", event.getAggregateType());
    }
}