package com.nexuspay.web.controller;

import com.nexuspay.service.RefundService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/refunds")
@RequiredArgsConstructor
public class RefundController {
    
    private final RefundService refundService;
    
    @PostMapping
    public ResponseEntity<?> create(
            @RequestAttribute("merchantId") UUID merchantId,
            @RequestBody CreateRefundRequest req) {
        return ResponseEntity.ok(refundService.create(merchantId,
                new RefundService.CreateRefundRequest(req.paymentIntentId(), req.amount(), req.reason())));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable UUID id) {
        return ResponseEntity.ok(refundService.getRefund(id));
    }
    
    @GetMapping
    public ResponseEntity<?> list(@RequestAttribute("merchantId") UUID merchantId) {
        return ResponseEntity.ok(refundService.listRefunds(merchantId));
    }
    
    public record CreateRefundRequest(
            @NotNull UUID paymentIntentId,
            BigInteger amount,
            com.nexuspay.domain.entity.Refund.RefundReason reason) {}
}
