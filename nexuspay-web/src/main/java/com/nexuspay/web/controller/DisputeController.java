package com.nexuspay.web.controller;

import com.nexuspay.service.DisputeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/merchants/{merchantId}/disputes")
@RequiredArgsConstructor
public class DisputeController {
    
    private final DisputeService disputeService;
    
    @GetMapping
    public ResponseEntity<?> list(@PathVariable UUID merchantId) {
        return ResponseEntity.ok(disputeService.listDisputes(merchantId));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable UUID merchantId, @PathVariable UUID id) {
        return ResponseEntity.ok(disputeService.getDispute(merchantId, id));
    }
    
    @PutMapping("/{id}/evidence")
    public ResponseEntity<?> saveEvidence(
            @PathVariable UUID merchantId,
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(disputeService.saveEvidence(merchantId, id, body.get("evidence")));
    }
    
    @PostMapping("/{id}/evidence/submit")
    public ResponseEntity<?> submitEvidence(@PathVariable UUID merchantId, @PathVariable UUID id) {
        return ResponseEntity.ok(disputeService.submitEvidence(merchantId, id));
    }
}
