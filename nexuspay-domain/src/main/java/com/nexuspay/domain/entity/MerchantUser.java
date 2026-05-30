package com.nexuspay.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "merchant_users", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "merchant_id"})
})
public class MerchantUser {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Role role;
    
    @Column(name = "invited_by")
    private UUID invitedBy;
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MemberStatus status = MemberStatus.ACTIVE;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    public enum Role {
        OWNER, ADMIN, DEVELOPER, FINANCE, VIEWER
    }
    
    public enum MemberStatus {
        ACTIVE, PENDING_INVITE, INACTIVE
    }
}
