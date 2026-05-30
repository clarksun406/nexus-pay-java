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
@Table(name = "routing_rules")
public class RoutingRule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;
    
    @Column(nullable = false)
    private Integer priority;
    
    @Column(nullable = false)
    private Boolean enabled = true;
    
    @Column(columnDefinition = "TEXT")
    private String currencies;
    
    @Column(name = "amount_min")
    private BigInteger amountMin;
    
    @Column(name = "amount_max")
    private BigInteger amountMax;
    
    @Column(name = "country_codes", columnDefinition = "TEXT")
    private String countryCodes;
    
    @Column(name = "payment_method_types", columnDefinition = "TEXT")
    private String paymentMethodTypes;
    
    @Column(name = "target_provider", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PaymentIntent.Provider targetProvider;
    
    @Column(name = "target_account_id")
    private UUID targetAccountId;
    
    @Column(name = "fallback_provider", length = 20)
    @Enumerated(EnumType.STRING)
    private PaymentIntent.Provider fallbackProvider;
    
    @Column(name = "fallback_account_id")
    private UUID fallbackAccountId;
    
    @Column(nullable = false)
    private Integer weight = 1;
    
    @Column(name = "max_cost_bps")
    private Integer maxCostBps;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
