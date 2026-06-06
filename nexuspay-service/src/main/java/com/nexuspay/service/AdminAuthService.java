package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.common.util.CryptoUtil;
import com.nexuspay.common.util.JwtUtil;
import com.nexuspay.domain.entity.RefreshToken;
import com.nexuspay.domain.entity.User;
import com.nexuspay.domain.entity.UserRole;
import com.nexuspay.repository.RefreshTokenRepository;
import com.nexuspay.repository.UserRepository;
import com.nexuspay.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CryptoUtil cryptoUtil;

    @Transactional
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new BusinessException("Invalid credentials", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new BusinessException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }

        List<UserRole> roles = userRoleRepository.findByUserId(user.getId());
        boolean hasAdminAccess = roles.stream()
                .anyMatch(r -> "SYSTEM".equals(r.getScopeType()) || "ORGANIZATION".equals(r.getScopeType()));

        if (!hasAdminAccess) {
            throw new BusinessException("No admin access", HttpStatus.FORBIDDEN);
        }

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refresh(String refreshToken) {
        var tokenHash = cryptoUtil.hashSha256(refreshToken, "refresh");
        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException("Invalid refresh token", HttpStatus.UNAUTHORIZED));

        if (stored.getRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException("Refresh token expired or revoked", HttpStatus.UNAUTHORIZED);
        }

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));

        refreshTokenRepository.deleteByUserId(user.getId());
        return buildAuthResponse(user);
    }

    @Transactional
    public void logout(UUID userId) {
        refreshTokenRepository.deleteByUserId(user.getId());
    }

    public Set<String> getPermissions(UUID userId) {
        List<UserRole> roles = userRoleRepository.findByUserId(userId);
        // Collect permissions via role_permissions join
        // For simplicity, return role codes as permission indicators
        return roles.stream()
                .map(ur -> "ROLE_" + ur.getScopeType())
                .collect(Collectors.toSet());
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtUtil.generateAdminAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), "ROLE_ADMIN");

        RefreshToken stored = new RefreshToken();
        stored.setUserId(user.getId());
        stored.setTokenHash(cryptoUtil.hashSha256(refreshToken, "refresh"));
        stored.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        refreshTokenRepository.save(stored);

        return new AuthResponse(accessToken, refreshToken, user.getId(), user.getEmail());
    }

    public record LoginRequest(String email, String password) {}
    public record AuthResponse(String accessToken, String refreshToken, UUID userId, String email) {}
}