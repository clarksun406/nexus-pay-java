package com.nexuspay.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "payouts")
public class Payout {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;
    
    @Column(name = "connector_id")
    private UUID connectorId;
    
    @Column(nullable = false, length = 10)
    private String currency;
    
    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private PaymentIntent.Mode mode = PaymentIntent.Mode.TEST;
    
    @Column(nullable = false)
    private BigInteger amount;
    
    @Column(name = "fee_amount")
    private BigInteger feeAmount;
    
    @Column(name = "net_amount")
    private BigInteger netAmount;
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PayoutStatus status = PayoutStatus.PENDING;
    
    @Column(name = "period_start", nullable = false)
    private Instant periodStart;
    
    @Column(name = "period_end", nullable = false)
    private Instant periodEnd;
    
    @Column(name = "items_count")
    private Integer itemsCount;
    
    @Column(name = "provider_payout_id", length = 255)
    private String providerPayoutId;
    
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 255)
    private String idempotencyKey;
    
    @Column(columnDefinition = "TEXT")
    @JdbcTypeCode(SqlTypes.JSON)
    private String breakdown;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "settled_at")
    private Instant settledAt;
    
    public enum PayoutStatus {
        PENDING, PROCESSING, PAID, FAILED, CANCELED
    }
}
