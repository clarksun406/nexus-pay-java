package com.nexuspay.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "payment_requests")
public class PaymentRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "payment_intent_id", nullable = false)
    private UUID paymentIntentId;
    
    @Column(name = "connector_account_id")
    private UUID connectorAccountId;
    
    @Column(nullable = false)
    private BigInteger amount;
    
    @Column(nullable = false, length = 10)
    private String currency;
    
    @Column(name = "payment_method_type", nullable = false, length = 50)
    private String paymentMethodType;
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.PENDING;
    
    @Column(name = "provider_request_id", length = 255)
    private String providerRequestId;
    
    @Column(name = "provider_response", columnDefinition = "TEXT")
    private String providerResponse;
    
    @Column(name = "failure_code", length = 100)
    private String failureCode;
    
    @Column(name = "failure_message", length = 500)
    private String failureMessage;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    public enum RequestStatus {
        PENDING, PROCESSING, SUCCEEDED, FAILED, CANCELED
    }
}
