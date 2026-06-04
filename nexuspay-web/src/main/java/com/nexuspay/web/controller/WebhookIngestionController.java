package com.nexuspay.web.controller;

import com.nexuspay.service.ProviderWebhookService;
import com.nexuspay.service.provider.ProviderWebhookAclPort;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
public class WebhookIngestionController {

    private final ProviderWebhookAclPort providerWebhookAclPort;
    private final ProviderWebhookService providerWebhookService;

    @PostMapping("/stripe")
    public ResponseEntity<?> stripeWebhook(
            @RequestHeader("Stripe-Signature") String signature,
            @RequestBody String payload) {

        if (!providerWebhookAclPort.verifyStripe(payload, signature)) {
            return ResponseEntity.badRequest().body("Invalid Stripe signature");
        }

        providerWebhookService.handleStripe(payload);
        log.info("Processed Stripe webhook (verified)");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/square")
    public ResponseEntity<?> squareWebhook(
            @RequestHeader("X-Square-Signature") String signature,
            @RequestBody String payload,
            HttpServletRequest request) {

        String notificationUrl = request.getRequestURL().toString();
        if (!providerWebhookAclPort.verifySquare(payload, signature, notificationUrl)) {
            return ResponseEntity.badRequest().body("Invalid Square signature");
        }

        providerWebhookService.handleSquare(payload);
        log.info("Processed Square webhook (verified)");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/braintree")
    public ResponseEntity<?> braintreeWebhook(
            @RequestParam(required = false) String bt_challenge,
            @RequestBody(required = false) String payload) {

        if (bt_challenge != null) {
            String verification = providerWebhookAclPort.generateBraintreeVerification(bt_challenge);
            if (verification == null || verification.isBlank()) {
                return ResponseEntity.badRequest().body("Invalid Braintree challenge");
            }
            return ResponseEntity.ok(verification);
        }

        log.info("Received Braintree webhook");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/braintree")
    public ResponseEntity<?> braintreeVerification(@RequestParam String bt_challenge) {
        String verification = providerWebhookAclPort.generateBraintreeVerification(bt_challenge);
        if (verification == null || verification.isBlank()) {
            return ResponseEntity.badRequest().body("Invalid Braintree challenge");
        }
        return ResponseEntity.ok(verification);
    }
}
