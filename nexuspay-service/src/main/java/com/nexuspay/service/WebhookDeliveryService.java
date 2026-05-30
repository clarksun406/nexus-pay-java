package com.nexuspay.service;

import com.nexuspay.common.util.CryptoUtil;
import com.nexuspay.domain.entity.WebhookEndpoint;
import com.nexuspay.repository.WebhookEndpointRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookDeliveryService {
    
    private final WebhookEndpointRepository webhookEndpointRepository;
    private final CryptoUtil cryptoUtil;
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Data
    public static class WebhookPayload {
        private String eventType;
        private UUID paymentIntentId;
        private String status;
        private Long timestamp;
    }
    
    public void sendWebhook(UUID merchantId, String eventType, UUID paymentIntentId, String status) {
        List<WebhookEndpoint> endpoints = webhookEndpointRepository.findByMerchantId(merchantId);
        
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
                log.error("Failed to deliver webhook to {}: {}", endpoint.getUrl(), e.getMessage());
            }
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
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Webhook-Signature", signature);
        headers.set("X-Webhook-Event", eventType);
        
        HttpEntity<WebhookPayload> request = new HttpEntity<>(payload, headers);
        
        restTemplate.postForEntity(endpoint.getUrl(), request, String.class);
        
        log.info("Webhook delivered to {}: event={}", endpoint.getUrl(), eventType);
    }
    
    private String signPayload(WebhookPayload payload, String secret) {
        return cryptoUtil.hashSha256(payload.toString(), secret);
    }
}
