package com.nexuspay.web.controller;

import com.nexuspay.service.RoutingRuleService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/routing-rules")
@RequiredArgsConstructor
public class RoutingRuleController {
    
    private final RoutingRuleService routingRuleService;
    
    @PostMapping
    public ResponseEntity<?> create(
            @RequestAttribute("merchantId") UUID merchantId,
            @RequestBody CreateRoutingRuleRequest req) {
        return ResponseEntity.ok(routingRuleService.create(merchantId,
                new RoutingRuleService.CreateRuleRequest(
                        req.priority(), req.enabled(), req.currencies(), req.amountMin(), req.amountMax(),
                        req.countryCodes(), req.paymentMethodTypes(), req.targetProvider(), req.targetAccountId(),
                        req.fallbackProvider(), req.fallbackAccountId(), req.weight())));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @RequestAttribute("merchantId") UUID merchantId,
            @PathVariable UUID id,
            @RequestBody UpdateRoutingRuleRequest req) {
        return ResponseEntity.ok(routingRuleService.update(merchantId, id,
                new RoutingRuleService.UpdateRuleRequest(
                        req.priority(), req.enabled(), req.currencies(), req.amountMin(), req.amountMax(),
                        req.countryCodes(), req.paymentMethodTypes(), req.weight())));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> get(
            @RequestAttribute("merchantId") UUID merchantId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(routingRuleService.getRule(merchantId, id));
    }
    
    @GetMapping
    public ResponseEntity<?> list(@RequestAttribute("merchantId") UUID merchantId) {
        return ResponseEntity.ok(routingRuleService.listRules(merchantId));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @RequestAttribute("merchantId") UUID merchantId,
            @PathVariable UUID id) {
        routingRuleService.delete(merchantId, id);
        return ResponseEntity.ok().build();
    }
    
    public record CreateRoutingRuleRequest(
            @NotNull Integer priority,
            Boolean enabled,
            String currencies,
            BigInteger amountMin,
            BigInteger amountMax,
            String countryCodes,
            String paymentMethodTypes,
            @NotNull com.nexuspay.domain.entity.PaymentIntent.Provider targetProvider,
            UUID targetAccountId,
            com.nexuspay.domain.entity.PaymentIntent.Provider fallbackProvider,
            UUID fallbackAccountId,
            Integer weight) {}
    
    public record UpdateRoutingRuleRequest(
            Integer priority,
            Boolean enabled,
            String currencies,
            BigInteger amountMin,
            BigInteger amountMax,
            String countryCodes,
            String paymentMethodTypes,
            Integer weight) {}
}
