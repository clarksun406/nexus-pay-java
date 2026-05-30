package com.nexuspay.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "gateway_logs")
public class GatewayLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "merchant_id")
    private UUID merchantId;
    
    @Column(name = "trace_id", length = 64)
    private String traceId;
    
    @Column(nullable = false, length = 10)
    private String method;
    
    @Column(nullable = false, length = 500)
    private String path;
    
    @Column(nullable = false)
    private Integer statusCode;
    
    @Column(columnDefinition = "TEXT")
    private String requestBody;
    
    @Column(columnDefinition = "TEXT")
    private String responseBody;
    
    @Column(nullable = false)
    private Long durationMs;
    
    @Column(length = 100)
    private String error;
    
    @Column(name = "user_id")
    private UUID userId;
    
    @Column(name = "api_key_id")
    private UUID apiKeyId;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
