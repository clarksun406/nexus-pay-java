package com.nexuspay.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "vault_audit_logs")
public class VaultAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "vault_entry_id")
    private UUID vaultEntryId;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private Action action;

    @Column(name = "actor_id")
    private UUID actorId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public enum Action {
        TOKENIZE, DETOKENIZE, READ, REVOKE
    }
}
