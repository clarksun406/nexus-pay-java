package com.nexuspay.service;

import com.nexuspay.domain.entity.OutboxEvent;
import com.nexuspay.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
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
    
    @Transactional
    public OutboxEvent createEvent(String aggregateType, UUID aggregateId, String eventType, String payload) {
        OutboxEvent event = new OutboxEvent();
        event.setAggregateType(aggregateType);
        event.setAggregateId(aggregateId);
        event.setEventType(eventType);
        event.setPayload(payload);
        return outboxEventRepository.save(event);
    }
    
    @Scheduled(fixedRate = 5000)
    @Transactional
    public void processOutbox() {
        List<OutboxEvent> pending = outboxEventRepository.findByStatusOrderByCreatedAtAsc(
            OutboxEvent.EventStatus.PENDING);
        
        for (OutboxEvent event : pending) {
            try {
                event.setStatus(OutboxEvent.EventStatus.PROCESSING);
                outboxEventRepository.save(event);
                
                // Trigger webhook delivery
                webhookDeliveryService.sendWebhook(
                    null, event.getEventType(), event.getAggregateId(), null);
                
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
}
