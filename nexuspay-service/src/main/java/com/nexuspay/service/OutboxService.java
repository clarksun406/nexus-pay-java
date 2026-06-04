package com.nexuspay.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexuspay.domain.entity.OutboxEvent;
import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {
    
    private final OutboxEventRepository outboxEventRepository;
    private final WebhookDeliveryService webhookDeliveryService;
    private final ObjectMapper objectMapper;
    
    @Transactional
    public OutboxEvent createEvent(String aggregateType, UUID aggregateId, String eventType, String payload) {
        OutboxEvent event = new OutboxEvent();
        event.setAggregateType(aggregateType);
        event.setAggregateId(aggregateId);
        event.setEventType(eventType);
        event.setPayload(payload);
        return outboxEventRepository.save(event);
    }

    @Transactional
    public OutboxEvent createPaymentEvent(PaymentIntent intent, String eventType) {
        String payload;
        try {
            payload = objectMapper.createObjectNode()
                    .put("merchantId", intent.getMerchantId().toString())
                    .put("paymentIntentId", intent.getId().toString())
                    .put("status", intent.getStatus().name())
                    .put("eventType", eventType)
                    .toString();
        } catch (Exception e) {
            payload = "{\"merchantId\":\"" + intent.getMerchantId() + "\",\"paymentIntentId\":\"" + intent.getId()
                    + "\",\"status\":\"" + intent.getStatus().name() + "\"}";
        }
        return createEvent("payment_intent", intent.getId(), eventType, payload);
    }
    
    @Transactional
    public void processOutbox() {
        List<OutboxEvent> pending = new java.util.ArrayList<>(
                outboxEventRepository.findByStatusOrderByCreatedAtAsc(OutboxEvent.EventStatus.PENDING));
        pending.addAll(outboxEventRepository.findByStatusAndNextRetryAtLessThanEqualOrderByCreatedAtAsc(
                OutboxEvent.EventStatus.FAILED, Instant.now()));
        
        for (OutboxEvent event : pending) {
            try {
                event.setStatus(OutboxEvent.EventStatus.PROCESSING);
                outboxEventRepository.save(event);
                
                UUID merchantId = extractUuid(event.getPayload(), "merchantId");
                UUID paymentIntentId = extractUuid(event.getPayload(), "paymentIntentId");
                String status = extractText(event.getPayload(), "status");
                if (merchantId == null) {
                    throw new IllegalStateException("Outbox event payload missing merchantId");
                }

                webhookDeliveryService.sendWebhook(
                    merchantId, event.getEventType(),
                    paymentIntentId != null ? paymentIntentId : event.getAggregateId(),
                    status);
                
                event.setStatus(OutboxEvent.EventStatus.DELIVERED);
                event.setDeliveredAt(Instant.now());
                outboxEventRepository.save(event);
                
            } catch (Exception e) {
                log.error("Failed to process outbox event {}: {}", event.getId(), e.getMessage());
                
                event.setStatus(OutboxEvent.EventStatus.FAILED);
                event.setError(e.getMessage());
                event.setRetryCount(event.getRetryCount() + 1);
                event.setNextRetryAt(Instant.now().plusSeconds(60));
                outboxEventRepository.save(event);
            }
        }
    }

    private UUID extractUuid(String payload, String field) {
        String value = extractText(payload, field);
        return value == null || value.isBlank() ? null : UUID.fromString(value);
    }

    private String extractText(String payload, String field) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            JsonNode value = node.path(field);
            return value.isMissingNode() || value.isNull() ? null : value.asText();
        } catch (Exception e) {
            return null;
        }
    }
}
