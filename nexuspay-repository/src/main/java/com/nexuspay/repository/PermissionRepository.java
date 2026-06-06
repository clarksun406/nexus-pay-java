package com.nexuspay.repository;

import com.nexuspay.domain.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    List<Permission> findByScope(String scope);

    @Query("SELECT DISTINCT p.code FROM Permission p " +
           "JOIN RolePermission rp ON rp.permissionId = p.id " +
           "JOIN UserRole ur ON ur.roleId = rp.roleId " +
           "WHERE ur.userId = :userId " +
           "AND (ur.scopeType = 'SYSTEM' " +
           "  OR (ur.scopeType = 'ORGANIZATION' AND ur.scopeId = :organizationId) " +
           "  OR (ur.scopeType = 'MERCHANT' AND ur.scopeId = :merchantId))")
    List<String> findPermissionCodesByUserAndScope(
            @Param("userId") UUID userId,
            @Param("organizationId") UUID organizationId,
            @Param("merchantId") UUID merchantId);
}