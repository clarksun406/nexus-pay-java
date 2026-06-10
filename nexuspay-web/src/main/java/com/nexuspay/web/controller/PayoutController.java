package com.nexuspay.web.controller;

import com.nexuspay.service.PayoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/merchants/{merchantId}/payouts")
@RequiredArgsConstructor
public class PayoutController {
    
    private final PayoutService payoutService;
    
    @GetMapping
    public ResponseEntity<?> list(@PathVariable UUID merchantId) {
        return ResponseEntity.ok(payoutService.listPayouts(merchantId));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable UUID merchantId, @PathVariable UUID id) {
        return ResponseEntity.ok(payoutService.getPayout(merchantId, id));
    }
}
