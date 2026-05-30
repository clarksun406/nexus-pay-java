package com.nexuspay.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "organization_users", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "organization_id"})
})
public class OrganizationUser {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private OrgRole role = OrgRole.ORG_MEMBER;
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private OrgUserStatus status = OrgUserStatus.ACTIVE;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    public enum OrgRole {
        ORG_OWNER, ORG_ADMIN, ORG_MEMBER
    }
    
    public enum OrgUserStatus {
        ACTIVE, INACTIVE
    }
}
