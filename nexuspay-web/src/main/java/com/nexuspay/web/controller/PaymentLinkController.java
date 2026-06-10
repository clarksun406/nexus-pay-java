package com.nexuspay.web.controller;

import com.nexuspay.service.PaymentLinkService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payment-links")
@RequiredArgsConstructor
public class PaymentLinkController {
    
    private final PaymentLinkService paymentLinkService;
    
    @PostMapping
    public ResponseEntity<?> create(
            @RequestAttribute("merchantId") UUID merchantId,
            @RequestBody CreatePaymentLinkRequest req) {
        return ResponseEntity.ok(paymentLinkService.create(merchantId,
                new PaymentLinkService.CreateRequest(req.title(), req.description(), req.amount(),
                        req.currency(), req.mode(), req.redirectUrl(), req.pinnedConnectorId())));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @RequestAttribute("merchantId") UUID merchantId,
            @PathVariable UUID id,
            @RequestBody UpdatePaymentLinkRequest req) {
        return ResponseEntity.ok(paymentLinkService.update(merchantId, id,
                new PaymentLinkService.UpdateRequest(req.title(), req.description(), 
                        req.amount(), req.status())));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deactivate(
            @RequestAttribute("merchantId") UUID merchantId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(paymentLinkService.deactivate(merchantId, id));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> get(
            @RequestAttribute("merchantId") UUID merchantId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(paymentLinkService.get(merchantId, id));
    }
    
    @GetMapping
    public ResponseEntity<?> list(@RequestAttribute("merchantId") UUID merchantId) {
        return ResponseEntity.ok(paymentLinkService.list(merchantId));
    }
    
    public record CreatePaymentLinkRequest(
            @NotBlank String title,
            String description,
            @NotNull BigInteger amount,
            String currency,
            com.nexuspay.domain.entity.PaymentIntent.Mode mode,
            String redirectUrl,
            UUID pinnedConnectorId) {}
    
    public record UpdatePaymentLinkRequest(
            String title,
            String description,
            BigInteger amount,
            com.nexuspay.domain.entity.PaymentLink.LinkStatus status) {}
}
