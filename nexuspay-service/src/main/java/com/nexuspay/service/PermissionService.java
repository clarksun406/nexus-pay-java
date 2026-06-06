package com.nexuspay.service;

import com.nexuspay.domain.entity.UserRole;
import com.nexuspay.repository.PermissionRepository;
import com.nexuspay.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final UserRoleRepository userRoleRepository;

    /**
     * Get all effective permission codes for a user, considering
     * their system, organization, and merchant role grants.
     */
    public Set<String> getEffectivePermissions(UUID userId) {
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        Set<String> permissions = new HashSet<>();

        for (UserRole ur : userRoles) {
            List<String> codes = permissionRepository.findPermissionCodesByUserAndScope(
                    userId, ur.getScopeId(), ur.getScopeId());
            permissions.addAll(codes);
        }
        return permissions;
    }

    /**
     * Check if a user holds a specific permission.
     */
    public boolean hasPermission(UUID userId, String permissionCode) {
        return getEffectivePermissions(userId).contains(permissionCode);
    }

    /**
     * Check if a user holds all specified permissions.
     */
    public boolean hasAllPermissions(UUID userId, String... permissionCodes) {
        Set<String> effective = getEffectivePermissions(userId);
        for (String code : permissionCodes) {
            if (!effective.contains(code)) return false;
        }
        return true;
    }

    /**
     * Check if a user holds any of the specified permissions.
     */
    public boolean hasAnyPermission(UUID userId, String... permissionCodes) {
        Set<String> effective = getEffectivePermissions(userId);
        for (String code : permissionCodes) {
            if (effective.contains(code)) return true;
        }
        return false;
    }
}