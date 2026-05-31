package com.nexuspay.web.controller;

import com.nexuspay.domain.entity.Subscription;
import com.nexuspay.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/merchants/{merchantId}/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {
    
    private final SubscriptionService subscriptionService;
    
    @PostMapping
    public ResponseEntity<Subscription> create(
            @PathVariable UUID merchantId,
            @RequestBody SubscriptionService.CreateRequest req) {
        return ResponseEntity.ok(subscriptionService.create(merchantId, req));
    }
    
    @GetMapping
    public ResponseEntity<List<Subscription>> list(@PathVariable UUID merchantId) {
        return ResponseEntity.ok(subscriptionService.listSubscriptions(merchantId));
    }
    
    @GetMapping("/{subscriptionId}")
    public ResponseEntity<Subscription> get(
            @PathVariable UUID merchantId,
            @PathVariable UUID subscriptionId) {
        return ResponseEntity.ok(subscriptionService.getSubscription(merchantId, subscriptionId));
    }
    
    @PostMapping("/{subscriptionId}/activate")
    public ResponseEntity<Subscription> activate(
            @PathVariable UUID merchantId,
            @PathVariable UUID subscriptionId) {
        return ResponseEntity.ok(subscriptionService.activate(merchantId, subscriptionId));
    }
    
    @PostMapping("/{subscriptionId}/cancel")
    public ResponseEntity<Subscription> cancel(
            @PathVariable UUID merchantId,
            @PathVariable UUID subscriptionId,
            @RequestParam(defaultValue = "false") boolean immediately) {
        return ResponseEntity.ok(subscriptionService.cancel(merchantId, subscriptionId, immediately));
    }
}
