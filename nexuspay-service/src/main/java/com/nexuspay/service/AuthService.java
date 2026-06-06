package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.common.util.CryptoUtil;
import com.nexuspay.common.util.JwtUtil;
import com.nexuspay.domain.entity.*;
import com.nexuspay.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantUserRepository merchantUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CryptoUtil cryptoUtil;
    private final EmailService emailService;
    
    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new BusinessException("Email already registered", HttpStatus.CONFLICT);
        }
        
        User user = new User();
        user.setEmail(req.email());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user = userRepository.save(user);
        
        Organization org = new Organization();
        org.setName(req.organizationName());
        org = organizationRepository.save(org);
        
        Merchant merchant = new Merchant();
        merchant.setOrganizationId(org.getId());
        merchant.setName(req.merchantName());
        merchant = merchantRepository.save(merchant);
        
        MerchantUser merchantUser = new MerchantUser();
        merchantUser.setUserId(user.getId());
        merchantUser.setMerchantId(merchant.getId());
        merchantUser.setRole(MerchantUser.Role.OWNER);
        merchantUser.setStatus(MerchantUser.MemberStatus.ACTIVE);
        merchantUserRepository.save(merchantUser);
        
        emailService.sendWelcomeEmail(user.getEmail(), merchant.getName());
        
        return buildAuthResponse(user, merchant);
    }
    
    @Transactional
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new BusinessException("Invalid credentials", HttpStatus.UNAUTHORIZED));
        
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new BusinessException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }
        
        MerchantUser merchantUser = merchantUserRepository.findByUserId(user.getId())
                .stream()
                .filter(mu -> mu.getStatus() == MerchantUser.MemberStatus.ACTIVE)
                .findFirst()
                .orElseThrow(() -> new BusinessException("No active merchant membership", HttpStatus.FORBIDDEN));
        
        Merchant merchant = merchantRepository.findById(merchantUser.getMerchantId())
                .orElseThrow(() -> new BusinessException("Merchant not found", HttpStatus.NOT_FOUND));
        
        return buildAuthResponse(user, merchant);
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
        
        MerchantUser merchantUser = merchantUserRepository.findByUserId(user.getId())
                .stream()
                .filter(mu -> mu.getStatus() == MerchantUser.MemberStatus.ACTIVE)
                .findFirst()
                .orElseThrow(() -> new BusinessException("No active merchant membership", HttpStatus.FORBIDDEN));
        
        Merchant merchant = merchantRepository.findById(merchantUser.getMerchantId())
                .orElseThrow(() -> new BusinessException("Merchant not found", HttpStatus.NOT_FOUND));
        
        refreshTokenRepository.deleteByUserId(user.getId());
        return buildAuthResponse(user, merchant);
    }
    
    @Transactional
    public void logout(UUID userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
    
    private AuthResponse buildAuthResponse(User user, Merchant merchant) {
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
        
        RefreshToken stored = new RefreshToken();
        stored.setUserId(user.getId());
        stored.setTokenHash(cryptoUtil.hashSha256(refreshToken, "refresh"));
        stored.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        refreshTokenRepository.save(stored);
        
        return new AuthResponse(accessToken, refreshToken, user.getId(), user.getEmail(), merchant.getId(), merchant.getName());
    }
    
    public record RegisterRequest(String email, String password, String organizationName, String merchantName) {}
    public record LoginRequest(String email, String password) {}
    public record AuthResponse(String accessToken, String refreshToken, UUID userId, String email, UUID merchantId, String merchantName) {}
}
