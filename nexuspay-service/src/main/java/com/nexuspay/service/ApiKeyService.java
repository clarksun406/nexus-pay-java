package com.nexuspay.service;

import com.nexuspay.common.util.CryptoUtil;
import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.domain.entity.ApiKey;
import com.nexuspay.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public CreatedApiKey create(UUID merchantId, ApiKey.Mode mode, ApiKey.KeyType type, String name) {
        ApiKey.Mode resolvedMode = mode != null ? mode : ApiKey.Mode.TEST;
        ApiKey.KeyType resolvedType = type != null ? type : ApiKey.KeyType.SECRET;
        String prefix = resolvedType == ApiKey.KeyType.SECRET ? "sk_" : "pk_";
        String suffix = resolvedMode == ApiKey.Mode.LIVE ? "live_" : "test_";
        String plainKey = prefix + suffix + cryptoUtil.generateToken();
        
        ApiKey key = new ApiKey();
        key.setMerchantId(merchantId);
        key.setMode(resolvedMode);
        key.setType(resolvedType);
        key.setKeyHash(hashKey(plainKey));
        key.setPrefix(plainKey.substring(0, 10));
        key.setName(name);
        
        ApiKey saved = apiKeyRepository.save(key);
        return new CreatedApiKey(
                saved.getId(),
                saved.getMode(),
                saved.getType(),
                saved.getPrefix(),
                saved.getName(),
                saved.getStatus(),
                saved.getCreatedAt(),
                plainKey);
    }
    
    public List<ApiKeySummary> listApiKeys(UUID merchantId) {
        return apiKeyRepository.findByMerchantId(merchantId).stream()
                .map(ApiKeyService::toSummary)
                .toList();
    }
    
    @Transactional
    public void revoke(UUID merchantId, UUID keyId) {
        ApiKey key = apiKeyRepository.findByMerchantIdAndId(merchantId, keyId)
                .orElseThrow(() -> new BusinessException("API key not found", HttpStatus.NOT_FOUND));
        key.setStatus(ApiKey.KeyStatus.REVOKED);
        key.setRevokedAt(Instant.now());
        apiKeyRepository.save(key);
    }

    private static ApiKeySummary toSummary(ApiKey key) {
        return new ApiKeySummary(
                key.getId(),
                key.getMode(),
                key.getType(),
                key.getPrefix(),
                key.getName(),
                key.getStatus(),
                key.getLastUsedAt(),
                key.getCreatedAt(),
                key.getRevokedAt());
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

    public record CreatedApiKey(UUID id, ApiKey.Mode mode, ApiKey.KeyType type, String prefix,
                                String name, ApiKey.KeyStatus status, Instant createdAt, String key) {}

    public record ApiKeySummary(UUID id, ApiKey.Mode mode, ApiKey.KeyType type, String prefix,
                                String name, ApiKey.KeyStatus status, Instant lastUsedAt,
                                Instant createdAt, Instant revokedAt) {}
}
