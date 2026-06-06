package com.nexuspay.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "vault_entries")
public class VaultEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "entry_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private EntryType entryType;

    @Column(nullable = false, unique = true, length = 255)
    private String token;

    @Column(name = "token_hash", nullable = false, unique = true, length = 128)
    private String tokenHash;

    @Column(name = "encrypted_data", nullable = false, columnDefinition = "TEXT")
    private String encryptedData;

    @Column(name = "encrypted_data_key", nullable = false, columnDefinition = "TEXT")
    private String encryptedDataKey;

    @Column(name = "key_id", nullable = false, length = 100)
    private String keyId;

    @Column(name = "encryption_algorithm", nullable = false, length = 50)
    private String encryptionAlgorithm;

    @Column(name = "key_model", nullable = false, length = 50)
    private String keyModel;

    @Column(name = "data_signature", length = 128)
    private String dataSignature;

    @Column(length = 128)
    private String fingerprint;

    @Column(length = 50)
    private String brand;

    @Column(name = "last4", length = 4)
    private String last4;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private EntryStatus status = EntryStatus.ACTIVE;

    @Column(columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private String metadata;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public enum EntryType {
        CARD, BANK_ACCOUNT, WALLET, GENERIC
    }

    public enum EntryStatus {
        ACTIVE, REVOKED, EXPIRED
    }
}
