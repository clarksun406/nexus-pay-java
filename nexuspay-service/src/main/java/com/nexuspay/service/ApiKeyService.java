package com.nexuspay.service;

import com.nexuspay.common.util.CryptoUtil;
import com.nexuspay.domain.entity.ApiKey;
import com.nexuspay.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApiKeyService {
    
    private final ApiKeyRepository apiKeyRepository;
    private final CryptoUtil cryptoUtil;
    
    @Transactional
    public ApiKey create(UUID merchantId, ApiKey.Mode mode, ApiKey.KeyType type, String name) {
        String prefix = type == ApiKey.KeyType.SECRET ? "sk_" : "pk_";
        String suffix = mode == ApiKey.Mode.LIVE ? "live_" : "test_";
        String plainKey = prefix + suffix + cryptoUtil.generateToken();
        
        ApiKey key = new ApiKey();
        key.setMerchantId(merchantId);
        key.setMode(mode);
        key.setType(type);
        key.setKeyHash(hashKey(plainKey));
        key.setPlaintextKey(plainKey);
        key.setPrefix(plainKey.substring(0, 10));
        key.setName(name);
        
        return apiKeyRepository.save(key);
    }
    
    public List<ApiKey> listApiKeys(UUID merchantId) {
        return apiKeyRepository.findByMerchantId(merchantId);
    }
    
    @Transactional
    public void revoke(UUID keyId) {
        apiKeyRepository.findById(keyId).ifPresent(key -> {
            key.setStatus(ApiKey.KeyStatus.REVOKED);
            key.setRevokedAt(Instant.now());
            apiKeyRepository.save(key);
        });
    }
    
    private String hashKey(String key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(key.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
