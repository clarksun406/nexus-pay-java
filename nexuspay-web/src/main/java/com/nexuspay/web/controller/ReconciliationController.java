package com.nexuspay.web.controller;

import com.nexuspay.service.ReconciliationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reconciliation")
@RequiredArgsConstructor
public class ReconciliationController {
    
    private final ReconciliationService reconciliationService;
    
    @PostMapping("/run")
    public ResponseEntity<?> runReconciliation(
            @RequestAttribute("merchantId") UUID merchantId,
            @RequestBody(required = false) ReconciliationRequest req) {
        
        Instant startTime = req != null && req.startTime() != null 
                ? Instant.parse(req.startTime()) 
                : Instant.now().minusSeconds(86400);
        Instant endTime = req != null && req.endTime() != null 
                ? Instant.parse(req.endTime()) 
                : Instant.now();
        
        return ResponseEntity.ok(reconciliationService.runReconciliation(merchantId, startTime, endTime));
    }
    
    @GetMapping("/discrepancies")
    public ResponseEntity<?> getOpenDiscrepancies(@RequestAttribute("merchantId") UUID merchantId) {
        return ResponseEntity.ok(Map.of(
                "discrepancies", reconciliationService.getOpenDiscrepancies(merchantId)
        ));
    }
    
    @PostMapping("/discrepancies/{paymentIntentId}/resolve")
    public ResponseEntity<?> resolveDiscrepancy(
            @PathVariable UUID paymentIntentId,
            @RequestBody ResolveRequest req) {
        reconciliationService.resolveDiscrepancy(paymentIntentId, req.resolution());
        return ResponseEntity.ok().build();
    }
    
    public record ReconciliationRequest(String startTime, String endTime) {}
    public record ResolveRequest(String resolution) {}
}
