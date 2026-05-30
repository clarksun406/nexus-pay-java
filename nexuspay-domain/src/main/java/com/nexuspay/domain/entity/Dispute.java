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
@Table(name = "disputes")
public class Dispute {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "payment_intent_id", nullable = false)
    private UUID paymentIntentId;
    
    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;
    
    @Column(name = "provider_dispute_id", nullable = false, length = 255)
    private String providerDisputeId;
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private DisputeStatus status = DisputeStatus.OPEN;
    
    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private DisputeReason reason;
    
    @Column(columnDefinition = "TEXT")
    private String evidence;
    
    @Column(name = "evidence_submitted_at")
    private Instant evidenceSubmittedAt;
    
    @Column(name = "due_by")
    private Instant dueBy;
    
    @Column(name = "amount")
    private java.math.BigInteger amount;
    
    @Column(name = "currency", length = 10)
    private String currency;
    
    @Column(name = "provider_response", columnDefinition = "TEXT")
    @JdbcTypeCode(SqlTypes.JSON)
    private String providerResponse;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    public enum DisputeStatus {
        OPEN, UNDER_REVIEW, WARNING_NEEDS_RESPONSE, WON, LOST, CHARGE_REFUNDED
    }
    
    public enum DisputeReason {
        FRAUDULENT, UNRECOGNIZED, PRODUCT_NOT_RECEIVED, 
        PRODUCT_UNACCEPTABLE, CREDIT_NOT_PROCESSED, OTHER
    }
}
