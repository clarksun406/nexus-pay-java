package com.nexuspay.web.controller;

import com.nexuspay.common.util.CryptoUtil;
import com.nexuspay.domain.entity.ApiKey;
import com.nexuspay.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/pub")
@RequiredArgsConstructor
public class PublicTokenizeController {
    
    private final ApiKeyRepository apiKeyRepository;
    private final CryptoUtil cryptoUtil;
    
    @PostMapping("/tokenize")
    public ResponseEntity<?> tokenize(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody TokenizeRequest req) {
        
        if (!authHeader.startsWith("pk_")) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid publishable key"));
        }
        
        String keyHash = hashKey(authHeader);
        ApiKey key = apiKeyRepository.findByKeyHash(keyHash)
                .filter(k -> k.getType() == ApiKey.KeyType.PUBLISHABLE)
                .filter(k -> k.getStatus() == ApiKey.KeyStatus.ACTIVE)
                .orElse(null);
        
        if (key == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid publishable key"));
        }
        
        // Generate gateway token
        String gwToken = "gw_tok_" + cryptoUtil.generateToken().substring(0, 24);
        
        return ResponseEntity.ok(Map.of(
                "token", gwToken,
                "merchantId", key.getMerchantId(),
                "provider", req.provider(),
                "providerPaymentMethod", req.providerPaymentMethod()
        ));
    }
    
    @GetMapping("/providers")
    public ResponseEntity<?> getProviders(@RequestHeader("Authorization") String authHeader) {
        if (!authHeader.startsWith("pk_")) {
            return ResponseEntity.status(401).build();
        }
        
        return ResponseEntity.ok(Map.of(
                "providers", java.util.List.of(
                    Map.of("name", "STRIPE", "publishableKey", "pk_test_xxx"),
                    Map.of("name", "SQUARE", "applicationId", "sandbox-xxx"),
                    Map.of("name", "BRAINTREE", "clientToken", "xxx")
                )
        ));
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
    
    public record TokenizeRequest(String provider, String providerPaymentMethod) {}
}
