package com.nexuspay.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RolePermissionRepository extends JpaRepository<com.nexuspay.domain.entity.RolePermission, UUID> {

    @Query("SELECT rp.permissionId FROM RolePermission rp WHERE rp.roleId = :roleId")
    List<UUID> findPermissionIdsByRoleId(@Param("roleId") UUID roleId);
}