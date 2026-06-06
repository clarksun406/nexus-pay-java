package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

/**
 * AOP aspect that enforces {@link RequirePermission} annotations.
 * Extracts userId from the request context and delegates to
 * {@link PermissionService} for authorization.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private final PermissionService permissionService;

    @Before("@annotation(requirePermission)")
    public void checkPermission(JoinPoint joinPoint, RequirePermission requirePermission) {
        UUID userId = extractUserId();
        if (userId == null) {
            throw new BusinessException("Not authenticated", HttpStatus.UNAUTHORIZED);
        }

        String[] permissions = requirePermission.value();
        boolean granted;

        if (requirePermission.any()) {
            granted = permissionService.hasAnyPermission(userId, permissions);
        } else {
            granted = permissionService.hasAllPermissions(userId, permissions);
        }

        if (!granted) {
            String method = ((MethodSignature) joinPoint.getSignature()).getMethod().getName();
            log.warn("Permission denied: user={} method={} required={}", userId, method, permissions);
            throw new BusinessException("Insufficient permissions", HttpStatus.FORBIDDEN);
        }
    }

    private UUID extractUserId() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;
            HttpServletRequest request = attrs.getRequest();
            Object userIdAttr = request.getAttribute("userId");
            return userIdAttr instanceof UUID uid ? uid : null;
        } catch (Exception e) {
            return null;
        }
    }
}