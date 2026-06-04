package com.nexuspay.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.repository.PaymentIntentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProviderWebhookService {

    private final PaymentIntentRepository paymentIntentRepository;
    private final OutboxService outboxService;
    private final ObjectMapper objectMapper;

    @Transactional
    public void handleStripe(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            String eventType = text(root, "type");
            JsonNode object = root.path("data").path("object");
            if (object.isMissingNode()) {
                log.warn("Stripe webhook without data.object: {}", eventType);
                return;
            }

            String providerPaymentId = text(object, "id");
            PaymentIntent.PaymentStatus status = stripeStatus(eventType, text(object, "status"));
            syncPayment(providerPaymentId, status, payload);
        } catch (Exception e) {
            log.warn("Failed to process Stripe webhook: {}", e.getMessage());
        }
    }

    @Transactional
    public void handleSquare(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            JsonNode object = root.path("data").path("object").path("payment");
            if (object.isMissingNode()) {
                object = root.path("data").path("object").path("refund");
            }
            if (object.isMissingNode()) {
                log.warn("Square webhook without payment/refund object");
                return;
            }

            String providerPaymentId = text(object, "id");
            PaymentIntent.PaymentStatus status = squareStatus(text(object, "status"));
            syncPayment(providerPaymentId, status, payload);
        } catch (Exception e) {
            log.warn("Failed to process Square webhook: {}", e.getMessage());
        }
    }

    private void syncPayment(String providerPaymentId, PaymentIntent.PaymentStatus status, String rawPayload) {
        if (providerPaymentId == null || status == null) {
            return;
        }

        paymentIntentRepository.findByProviderPaymentId(providerPaymentId).ifPresentOrElse(intent -> {
            PaymentIntent.PaymentStatus oldStatus = intent.getStatus();
            intent.setStatus(status);
            intent.setProviderResponse(rawPayload);
            paymentIntentRepository.save(intent);

            if (oldStatus != status) {
                outboxService.createPaymentEvent(intent, "payment_intent." + status.name().toLowerCase());
            }
        }, () -> log.warn("No local payment intent found for provider payment {}", providerPaymentId));
    }

    private PaymentIntent.PaymentStatus stripeStatus(String eventType, String providerStatus) {
        if ("payment_intent.succeeded".equals(eventType)) return PaymentIntent.PaymentStatus.SUCCEEDED;
        if ("payment_intent.canceled".equals(eventType)) return PaymentIntent.PaymentStatus.CANCELED;
        if ("payment_intent.payment_failed".equals(eventType)) return PaymentIntent.PaymentStatus.FAILED;
        if ("requires_capture".equals(providerStatus)) return PaymentIntent.PaymentStatus.REQUIRES_CAPTURE;
        if ("requires_action".equals(providerStatus)) return PaymentIntent.PaymentStatus.REQUIRES_ACTION;
        if ("processing".equals(providerStatus)) return PaymentIntent.PaymentStatus.PROCESSING;
        if ("succeeded".equals(providerStatus)) return PaymentIntent.PaymentStatus.SUCCEEDED;
        if ("canceled".equals(providerStatus)) return PaymentIntent.PaymentStatus.CANCELED;
        return null;
    }

    private PaymentIntent.PaymentStatus squareStatus(String providerStatus) {
        if ("COMPLETED".equals(providerStatus)) return PaymentIntent.PaymentStatus.SUCCEEDED;
        if ("CANCELED".equals(providerStatus)) return PaymentIntent.PaymentStatus.CANCELED;
        if ("APPROVED".equals(providerStatus)) return PaymentIntent.PaymentStatus.REQUIRES_CAPTURE;
        if ("PENDING".equals(providerStatus)) return PaymentIntent.PaymentStatus.PROCESSING;
        if ("FAILED".equals(providerStatus)) return PaymentIntent.PaymentStatus.FAILED;
        return null;
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }
}
