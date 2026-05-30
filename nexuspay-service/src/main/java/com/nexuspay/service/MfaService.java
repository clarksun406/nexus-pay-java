package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.domain.entity.User;
import com.nexuspay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base32;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MfaService {
    
    private final UserRepository userRepository;
    private final Base32 base32 = new Base32();
    
    @Transactional
    public MfaSetupResponse setupMfa(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));
        
        String secret = generateSecret();
        user.setMfaSecret(secret);
        user.setMfaEnabled(false);
        userRepository.save(user);
        
        String otpAuthUrl = String.format(
                "otpauth://totp/NexusPay:%s?secret=%s&issuer=NexusPay",
                user.getEmail(), secret);
        
        return new MfaSetupResponse(secret, otpAuthUrl);
    }
    
    @Transactional
    public boolean confirmMfa(UUID userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));
        
        if (user.getMfaSecret() == null) {
            throw new BusinessException("MFA not set up", HttpStatus.BAD_REQUEST);
        }
        
        if (!verifyCode(user.getMfaSecret(), code)) {
            throw new BusinessException("Invalid code", HttpStatus.BAD_REQUEST);
        }
        
        user.setMfaEnabled(true);
        userRepository.save(user);
        
        return true;
    }
    
    @Transactional
    public void disableMfa(UUID userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));
        
        if (!user.getMfaEnabled()) {
            throw new BusinessException("MFA not enabled", HttpStatus.BAD_REQUEST);
        }
        
        if (!verifyCode(user.getMfaSecret(), code)) {
            throw new BusinessException("Invalid code", HttpStatus.BAD_REQUEST);
        }
        
        user.setMfaEnabled(false);
        user.setMfaSecret(null);
        userRepository.save(user);
    }
    
    public boolean verifyMfa(UUID userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));
        
        if (!user.getMfaEnabled() || user.getMfaSecret() == null) {
            return true; // MFA not enabled
        }
        
        return verifyCode(user.getMfaSecret(), code);
    }
    
    private boolean verifyCode(String secret, String code) {
        try {
            long timeIndex = System.currentTimeMillis() / 1000 / 30;
            byte[] key = base32.decode(secret);
            
            for (int i = -1; i <= 1; i++) {
                byte[] expected = generateCode(key, timeIndex + i);
                byte[] actual = hexStringToByteArray(String.format("%016x", Long.parseLong(code)));
                
                if (Arrays.equals(expected, actual)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    private byte[] generateCode(byte[] key, long timeIndex) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(key, "HmacSHA1"));
        
        byte[] time = new byte[8];
        for (int i = 7; i >= 0; i--) {
            time[i] = (byte) timeIndex;
            timeIndex >>= 8;
        }
        
        byte[] hash = mac.doFinal(time);
        int offset = hash[hash.length - 1] & 0xf;
        
        byte[] code = new byte[4];
        for (int i = 0; i < 4; i++) {
            code[i] = hash[offset + i];
        }
        
        code[0] &= 0x7f;
        return Arrays.copyOf(code, 8);
    }
    
    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
    
    private String generateSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        return base32.encodeToString(bytes);
    }
    
    public record MfaSetupResponse(String secret, String otpAuthUrl) {}
}
