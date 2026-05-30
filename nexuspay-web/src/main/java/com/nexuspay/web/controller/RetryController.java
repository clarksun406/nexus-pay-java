package com.nexuspay.web.controller;

import com.nexuspay.service.DeclineCodeService;
import com.nexuspay.service.RetryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RetryController {
    
    private final RetryService retryService;
    private final DeclineCodeService declineCodeService;
    
    @GetMapping("/merchants/{merchantId}/retry-config")
    public ResponseEntity<?> getRetryConfig(@PathVariable UUID merchantId) {
        return ResponseEntity.ok(retryService.getRetryConfig(merchantId));
    }
    
    @PutMapping("/merchants/{merchantId}/retry-config")
    public ResponseEntity<?> updateRetryConfig(
            @PathVariable UUID merchantId,
            @RequestBody UpdateRetryConfigRequest req) {
        return ResponseEntity.ok(Map.of(
                "maxRetries", req.maxRetries(),
                "retryDelayMs", req.retryDelayMs(),
                "enableFallback", req.enableFallback()
        ));
    }
    
    @GetMapping("/merchants/{merchantId}/retry-stats")
    public ResponseEntity<?> getRetryStats(
            @PathVariable UUID merchantId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return ResponseEntity.ok(Map.of(
                "totalRetries", 0,
                "successfulRetries", 0,
                "failedRetries", 0
        ));
    }
    
    @GetMapping("/decline-codes")
    public ResponseEntity<?> getDeclineCodes(@RequestParam(required = false) String provider) {
        return ResponseEntity.ok(Map.of(
                "categories", declineCodeService.getAllCategories()
        ));
    }
    
    public record UpdateRetryConfigRequest(int maxRetries, long retryDelayMs, boolean enableFallback) {}
}
