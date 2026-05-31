package com.nexuspay.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "subscriptions")
public class Subscription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    
    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;
    
    @Column(name = "payment_method_id")
    private UUID paymentMethodId;
    
    @Column(name = "plan_id", length = 255)
    private String planId;
    
    @Column(length = 255)
    private String name;
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status = SubscriptionStatus.INCOMPLETE;
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SubscriptionInterval interval;
    
    @Column(name = "interval_count", nullable = false)
    private Integer intervalCount = 1;
    
    @Column(nullable = false)
    private Long amount;
    
    @Column(nullable = false, length = 10)
    private String currency;
    
    @Column(name = "trial_start")
    private Instant trialStart;
    
    @Column(name = "trial_end")
    private Instant trialEnd;
    
    @Column(name = "current_period_start")
    private Instant currentPeriodStart;
    
    @Column(name = "current_period_end")
    private Instant currentPeriodEnd;
    
    @Column(name = "canceled_at")
    private Instant canceledAt;
    
    @Column(name = "cancel_at_period_end")
    private Boolean cancelAtPeriodEnd = false;
    
    @Column(name = "provider_subscription_id", length = 255)
    private String providerSubscriptionId;
    
    @Column(columnDefinition = "TEXT")
    private String metadata;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    public enum SubscriptionStatus {
        ACTIVE, PAST_DUE, CANCELED, INCOMPLETE, TRIALING, UNPAID
    }
    
    public enum SubscriptionInterval {
        DAY, WEEK, MONTH, YEAR
    }
}
