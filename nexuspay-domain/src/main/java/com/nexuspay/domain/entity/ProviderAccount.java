package com.nexuspay.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "provider_accounts")
public class ProviderAccount {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;
    
    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private Provider provider;
    
    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private Mode mode = Mode.TEST;
    
    @Column(nullable = false, length = 100)
    private String label;
    
    @Column(name = "encrypted_secret_key", columnDefinition = "TEXT")
    private String encryptedSecretKey;
    
    @Column(name = "encrypted_publishable_key", columnDefinition = "TEXT")
    private String encryptedPublishableKey;
    
    @Column(columnDefinition = "TEXT")
    @JdbcTypeCode(SqlTypes.JSON)
    private String encryptedCredentials;
    
    @Column(name = "provider_config", columnDefinition = "TEXT")
    @JdbcTypeCode(SqlTypes.JSON)
    private String providerConfig;
    
    @Column(name = "secret_key_hint", length = 20)
    private String secretKeyHint;
    
    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;
    
    @Column(nullable = false)
    private Integer weight = 1;
    
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;
    
    @Column(name = "fee_config", columnDefinition = "TEXT")
    @JdbcTypeCode(SqlTypes.JSON)
    private String feeConfig;
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ConnectorStatus status = ConnectorStatus.ACTIVE;
    
    @Column(name = "connector_account_id", length = 255)
    private String connectorAccountId;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    public enum Provider {
        STRIPE, SQUARE, BRAINTREE
    }
    
    public enum Mode {
        TEST, LIVE
    }
    
    public enum ConnectorStatus {
        ACTIVE, INACTIVE, UNHEALTHY
    }
}
