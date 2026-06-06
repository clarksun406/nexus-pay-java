package com.nexuspay.repository;

import com.nexuspay.domain.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    List<UserRole> findByUserId(UUID userId);

    List<UserRole> findByUserIdAndScopeTypeAndScopeId(UUID userId, String scopeType, UUID scopeId);

    List<UserRole> findByUserIdAndScopeType(UUID userId, String scopeType);

    boolean existsByUserIdAndRoleIdAndScopeTypeAndScopeId(UUID userId, UUID roleId, String scopeType, UUID scopeId);

    void deleteByUserIdAndScopeTypeAndScopeId(UUID userId, String scopeType, UUID scopeId);
}