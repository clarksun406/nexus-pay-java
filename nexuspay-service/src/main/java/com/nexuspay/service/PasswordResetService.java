package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.common.util.CryptoUtil;
import com.nexuspay.domain.entity.PasswordResetToken;
import com.nexuspay.domain.entity.User;
import com.nexuspay.repository.PasswordResetTokenRepository;
import com.nexuspay.repository.RefreshTokenRepository;
import com.nexuspay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final CryptoUtil cryptoUtil;
    private final EmailService emailService;

    @Transactional
    public PasswordResetRequestResponse requestReset(String email) {
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return new PasswordResetRequestResponse(true, null);
        }

        User user = userOpt.get();
        passwordResetTokenRepository.deleteByUserId(user.getId());

        String rawToken = cryptoUtil.generateToken();
        String tokenHash = cryptoUtil.hashSha256(rawToken, "pwd_reset");

        PasswordResetToken token = new PasswordResetToken();
        token.setUserId(user.getId());
        token.setTokenHash(tokenHash);
        token.setExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES));
        token.setUsed(false);
        passwordResetTokenRepository.save(token);

        emailService.sendPasswordResetEmail(user.getEmail(), rawToken);

        return new PasswordResetRequestResponse(true, null);
    }

    @Transactional
    public boolean confirmReset(String token, String newPassword) {
        String tokenHash = cryptoUtil.hashSha256(token, "pwd_reset");
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException("Invalid reset token", HttpStatus.BAD_REQUEST));

        if (Boolean.TRUE.equals(resetToken.getUsed()) || resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException("Reset token expired or used", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setTokenVersion(user.getTokenVersion() == null ? 1 : user.getTokenVersion() + 1);
        userRepository.save(user);

        resetToken.setUsed(true);
        resetToken.setUsedAt(Instant.now());
        passwordResetTokenRepository.save(resetToken);

        refreshTokenRepository.deleteByUserId(user.getId());
        return true;
    }

    public record PasswordResetRequestResponse(boolean success, String resetToken) {}
}
