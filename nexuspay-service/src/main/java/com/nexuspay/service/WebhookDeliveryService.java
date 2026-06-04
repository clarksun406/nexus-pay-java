package com.nexuspay.service;

import com.nexuspay.common.util.CryptoUtil;
import com.nexuspay.domain.entity.WebhookEndpoint;
import com.nexuspay.repository.WebhookEndpointRepository;
import com.nexuspay.service.provider.WebhookTransportPort;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookDeliveryService {

    private final WebhookEndpointRepository webhookEndpointRepository;
    private final CryptoUtil cryptoUtil;
    private final WebhookTransportPort webhookTransportPort;

    @Data
    public static class WebhookPayload {
        private String eventType;
        private UUID paymentIntentId;
        private String status;
        private Long timestamp;
    }

    public void sendWebhook(UUID merchantId, String eventType, UUID paymentIntentId, String status) {
        List<WebhookEndpoint> endpoints = webhookEndpointRepository.findByMerchantId(merchantId);
        List<String> failures = new ArrayList<>();

        for (WebhookEndpoint endpoint : endpoints) {
            if (endpoint.getStatus() != WebhookEndpoint.EndpointStatus.ACTIVE) {
                continue;
            }

            if (endpoint.getEvents() != null && !endpoint.getEvents().contains(eventType)) {
                continue;
            }

            try {
                deliverWebhook(endpoint, eventType, paymentIntentId, status);
            } catch (Exception e) {
                log.error("Failed to deliver webhook to {}: {}", endpoint.getUrl(), e.getMessage(), e);
                failures.add(endpoint.getUrl() + ": " + e.getMessage());
            }
        }

        if (!failures.isEmpty()) {
            throw new IllegalStateException("Webhook delivery failed: " + String.join("; ", failures));
        }
    }

    private void deliverWebhook(WebhookEndpoint endpoint, String eventType,
                                UUID paymentIntentId, String status) {
        WebhookPayload payload = new WebhookPayload();
        payload.setEventType(eventType);
        payload.setPaymentIntentId(paymentIntentId);
        payload.setStatus(status);
        payload.setTimestamp(Instant.now().getEpochSecond());

        String signature = signPayload(payload, endpoint.getSigningSecret());

        Map<String, String> headers = new HashMap<>();
        headers.put("X-Webhook-Signature", signature);
        headers.put("X-Webhook-Event", eventType);

        webhookTransportPort.postJson(endpoint.getUrl(), payload, headers);

        log.info("Webhook delivered to {}: event={}", endpoint.getUrl(), eventType);
    }

    private String signPayload(WebhookPayload payload, String secret) {
        String canonical = String.join("|",
                safe(payload.getEventType()),
                String.valueOf(payload.getPaymentIntentId()),
                safe(payload.getStatus()),
                String.valueOf(payload.getTimestamp())
        );
        return cryptoUtil.hashSha256(canonical, secret);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
