package com.nexuspay.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "payment_intents", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"merchant_id", "idempotency_key"})
})
public class PaymentIntent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;
    
    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private Mode mode = Mode.TEST;
    
    @Column(nullable = false)
    private BigInteger amount;
    
    @Column(nullable = false, length = 10)
    private String currency;
    
    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.REQUIRES_PAYMENT_METHOD;
    
    @Column(name = "capture_method", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private CaptureMethod captureMethod = CaptureMethod.AUTOMATIC;
    
    @Column(name = "idempotency_key", nullable = false, length = 255)
    private String idempotencyKey;
    
    @Column(name = "resolved_provider", length = 20)
    @Enumerated(EnumType.STRING)
    private Provider resolvedProvider;
    
    @Column(name = "connector_account_id")
    private UUID connectorAccountId;
    
    @Column(name = "provider_payment_id", length = 255)
    private String providerPaymentId;
    
    @Column(name = "provider_response", columnDefinition = "TEXT")
    private String providerResponse;
    
    @Column(name = "payment_method_type", length = 50)
    private String paymentMethodType;
    
    @Column(columnDefinition = "TEXT")
    @JdbcTypeCode(SqlTypes.JSON)
    private String metadata;
    
    @Column(name = "order_id", length = 255)
    private String orderId;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "billing_details")
    @JdbcTypeCode(SqlTypes.JSON)
    private String billingDetails;
    
    @Column(name = "shipping_details")
    @JdbcTypeCode(SqlTypes.JSON)
    private String shippingDetails;
    
    @Column(name = "success_url", length = 500)
    private String successUrl;
    
    @Column(name = "cancel_url", length = 500)
    private String cancelUrl;
    
    @Column(name = "failure_url", length = 500)
    private String failureUrl;
    
    @Column(name = "three_ds_action_url", length = 1000)
    private String threeDsActionUrl;
    
    @Column(name = "trace_id", length = 64)
    private String traceId;
    
    @Column(name = "expires_at")
    private Instant expiresAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    public enum Mode {
        TEST, LIVE
    }
    
    public enum PaymentStatus {
        REQUIRES_PAYMENT_METHOD,
        REQUIRES_CONFIRMATION,
        REQUIRES_ACTION,
        PROCESSING,
        REQUIRES_CAPTURE,
        CANCELED,
        SUCCEEDED,
        FAILED
    }
    
    public enum CaptureMethod {
        AUTOMATIC, MANUAL
    }
    
    public enum Provider {
        STRIPE, SQUARE, BRAINTREE
    }
}
