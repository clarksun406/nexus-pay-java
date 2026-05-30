package com.nexuspay.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "payment_links")
public class PaymentLink {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;
    
    @Column(nullable = false, unique = true, length = 64)
    private String token;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private BigInteger amount;
    
    @Column(nullable = false, length = 10)
    private String currency = "usd";
    
    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private PaymentIntent.Mode mode = PaymentIntent.Mode.TEST;
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private LinkStatus status = LinkStatus.ACTIVE;
    
    @Column(name = "redirect_url", length = 500)
    private String redirectUrl;
    
    @Column(name = "pinned_connector_id")
    private UUID pinnedConnectorId;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "expires_at")
    private Instant expiresAt;
    
    public enum LinkStatus {
        ACTIVE, INACTIVE, EXPIRED
    }
}
