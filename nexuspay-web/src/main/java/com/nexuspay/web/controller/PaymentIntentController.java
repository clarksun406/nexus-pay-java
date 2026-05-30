package com.nexuspay.web.controller;

import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.service.PaymentIntentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payment-intents")
@RequiredArgsConstructor
public class PaymentIntentController {
    
    private final PaymentIntentService paymentIntentService;
    
    @PostMapping
    public ResponseEntity<?> create(
            @RequestAttribute("merchantId") UUID merchantId,
            @Valid @RequestBody CreatePaymentRequest req) {
        PaymentIntent intent = paymentIntentService.create(merchantId, 
                new PaymentIntentService.CreateRequest(
                        req.amount(), req.currency(), req.mode(), req.captureMethod(),
                        req.idempotencyKey(), req.metadata(), req.orderId(), req.description(),
                        req.successUrl(), req.cancelUrl()));
        return ResponseEntity.ok(intent);
    }
    
    @PostMapping("/{id}/confirm")
    public ResponseEntity<?> confirm(
            @RequestAttribute("merchantId") UUID merchantId,
            @PathVariable UUID id,
            @Valid @RequestBody ConfirmPaymentRequest req) {
        PaymentIntent intent = paymentIntentService.confirm(merchantId, id,
                new PaymentIntentService.ConfirmRequest(req.paymentMethodType(), req.paymentMethodId()));
        return ResponseEntity.ok(intent);
    }
    
    @PostMapping("/{id}/capture")
    public ResponseEntity<?> capture(
            @RequestAttribute("merchantId") UUID merchantId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(paymentIntentService.capture(merchantId, id));
    }
    
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(
            @RequestAttribute("merchantId") UUID merchantId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(paymentIntentService.cancel(merchantId, id));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> get(
            @RequestAttribute("merchantId") UUID merchantId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(paymentIntentService.getPaymentIntent(merchantId, id));
    }
    
    @GetMapping
    public ResponseEntity<?> list(@RequestAttribute("merchantId") UUID merchantId) {
        return ResponseEntity.ok(paymentIntentService.listPaymentIntents(merchantId));
    }
    
    public record CreatePaymentRequest(
            @NotNull @Positive BigInteger amount,
            @NotBlank String currency,
            PaymentIntent.Mode mode,
            PaymentIntent.CaptureMethod captureMethod,
            String idempotencyKey,
            String metadata,
            String orderId,
            String description,
            String successUrl,
            String cancelUrl) {}
    
    public record ConfirmPaymentRequest(
            @NotBlank String paymentMethodType,
            @NotBlank String paymentMethodId) {}
}
