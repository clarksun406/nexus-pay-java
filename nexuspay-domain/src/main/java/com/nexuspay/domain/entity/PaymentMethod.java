package com.nexuspay.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "payment_methods")
public class PaymentMethod {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    
    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;
    
    @Column(name = "provider_payment_method_id", length = 255, nullable = false)
    private String providerPaymentMethodId;
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PaymentMethodType type;
    
    @Column(length = 10)
    private String last4;
    
    @Column(length = 20)
    private String brand;
    
    @Column(name = "expiry_month")
    private Integer expiryMonth;
    
    @Column(name = "expiry_year")
    private Integer expiryYear;
    
    @Column(name = "card_holder_name", length = 255)
    private String cardHolderName;
    
    @Column(length = 20)
    private String fingerprint;
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PaymentMethodStatus status = PaymentMethodStatus.ACTIVE;
    
    @Column(name = "is_default")
    private Boolean isDefault = false;
    
    @Column(columnDefinition = "TEXT")
    private String metadata;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    public enum PaymentMethodType {
        CARD, ALIPAY, WECHAT_PAY, APPLE_PAY, GOOGLE_PAY
    }
    
    public enum PaymentMethodStatus {
        ACTIVE, INACTIVE, EXPIRED
    }
}
