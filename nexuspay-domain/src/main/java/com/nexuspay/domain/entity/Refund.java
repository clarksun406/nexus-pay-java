package com.nexuspay.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "refunds")
public class Refund {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "payment_intent_id", nullable = false)
    private UUID paymentIntentId;
    
    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;
    
    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private PaymentIntent.Mode mode = PaymentIntent.Mode.TEST;
    
    @Column(nullable = false)
    private BigInteger amount;
    
    @Column(nullable = false, length = 10)
    private String currency;
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RefundStatus status = RefundStatus.PENDING;
    
    @Column(length = 30)
    @Enumerated(EnumType.STRING)
    private RefundReason reason;
    
    @Column(name = "provider_refund_id", length = 255)
    private String providerRefundId;
    
    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    public enum RefundStatus {
        PENDING, SUCCEEDED, FAILED, CANCELED
    }
    
    public enum RefundReason {
        DUPLICATE, FRAUDULENT, REQUESTED_BY_CUSTOMER, OTHER
    }
}
