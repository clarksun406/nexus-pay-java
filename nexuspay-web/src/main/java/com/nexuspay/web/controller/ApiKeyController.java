package com.nexuspay.web.controller;

import com.nexuspay.domain.entity.ApiKey;
import com.nexuspay.service.ApiKeyService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {
    
    private final ApiKeyService apiKeyService;
    
    @PostMapping
    public ResponseEntity<?> create(
            @RequestAttribute("merchantId") UUID merchantId,
            @RequestBody CreateApiKeyRequest req) {
        return ResponseEntity.ok(apiKeyService.create(merchantId, req.mode(), req.type(), req.name()));
    }
    
    @GetMapping
    public ResponseEntity<?> list(@RequestAttribute("merchantId") UUID merchantId) {
        return ResponseEntity.ok(apiKeyService.listApiKeys(merchantId));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> revoke(
            @RequestAttribute("merchantId") UUID merchantId,
            @PathVariable UUID id) {
        apiKeyService.revoke(merchantId, id);
        return ResponseEntity.ok().build();
    }
    
    public record CreateApiKeyRequest(
            ApiKey.Mode mode,
            ApiKey.KeyType type,
            @NotBlank String name) {}
}
