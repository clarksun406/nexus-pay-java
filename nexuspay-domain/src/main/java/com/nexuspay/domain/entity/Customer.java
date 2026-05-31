package com.nexuspay.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "customers")
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;
    
    @Column(length = 255)
    private String email;
    
    @Column(length = 255)
    private String name;
    
    @Column(length = 255)
    private String phone;
    
    @Column(name = "provider_customer_id", length = 255)
    private String providerCustomerId;
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private CustomerStatus status = CustomerStatus.ACTIVE;
    
    @Column(columnDefinition = "TEXT")
    private String metadata;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    public enum CustomerStatus {
        ACTIVE, INACTIVE, DELETED
    }
}
