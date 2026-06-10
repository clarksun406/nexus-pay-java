package com.nexuspay.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "api_keys")
public class ApiKey {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;
    
    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private Mode mode;
    
    @Column(nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private KeyType type;
    
    @Column(name = "key_hash", nullable = false, unique = true, length = 64)
    private String keyHash;
    
    @Column(nullable = false, length = 20)
    private String prefix;
    
    @Column(length = 100)
    private String name;
    
    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private KeyStatus status = KeyStatus.ACTIVE;
    
    @Column(name = "last_used_at")
    private Instant lastUsedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "revoked_at")
    private Instant revokedAt;
    
    public enum Mode {
        TEST, LIVE
    }
    
    public enum KeyType {
        SECRET, PUBLISHABLE
    }
    
    public enum KeyStatus {
        ACTIVE, REVOKED
    }
}
