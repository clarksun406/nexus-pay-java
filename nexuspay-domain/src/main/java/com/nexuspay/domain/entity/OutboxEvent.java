package com.nexuspay.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "outbox_events")
public class OutboxEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType;
    
    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;
    
    @Column(nullable = false, length = 100)
    private String eventType;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private EventStatus status = EventStatus.PENDING;
    
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;
    
    @Column(name = "next_retry_at")
    private Instant nextRetryAt;
    
    @Column(name = "delivered_at")
    private Instant deliveredAt;
    
    @Column(columnDefinition = "TEXT")
    private String error;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    public enum EventStatus {
        PENDING, PROCESSING, DELIVERED, FAILED
    }
}
