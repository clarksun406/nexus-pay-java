package com.nexuspay.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "webhook_endpoints")
public class WebhookEndpoint {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;
    
    @Column(nullable = false, length = 500)
    private String url;
    
    @Column(columnDefinition = "TEXT")
    private String events;
    
    @Column(name = "signing_secret", length = 64)
    private String signingSecret;
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private EndpointStatus status = EndpointStatus.ACTIVE;
    
    @Column(name = "last_triggered_at")
    private Instant lastTriggeredAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    public enum EndpointStatus {
        ACTIVE, INACTIVE, DISABLED
    }
}
