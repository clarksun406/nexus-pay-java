package com.nexuspay.web.controller;

import com.nexuspay.service.WebhookService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookController {
    
    private final WebhookService webhookService;
    
    @PostMapping
    public ResponseEntity<?> create(
            @RequestAttribute("merchantId") UUID merchantId,
            @RequestBody CreateWebhookRequest req) {
        return ResponseEntity.ok(webhookService.create(merchantId,
                new WebhookService.CreateWebhookRequest(req.url(), req.events())));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @RequestAttribute("merchantId") UUID merchantId,
            @PathVariable UUID id,
            @RequestBody UpdateWebhookRequest req) {
        return ResponseEntity.ok(webhookService.update(merchantId, id,
                new WebhookService.UpdateWebhookRequest(req.url(), req.events(), req.status())));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> get(
            @RequestAttribute("merchantId") UUID merchantId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(webhookService.getEndpoint(merchantId, id));
    }
    
    @GetMapping
    public ResponseEntity<?> list(@RequestAttribute("merchantId") UUID merchantId) {
        return ResponseEntity.ok(webhookService.listEndpoints(merchantId));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @RequestAttribute("merchantId") UUID merchantId,
            @PathVariable UUID id) {
        webhookService.delete(merchantId, id);
        return ResponseEntity.ok().build();
    }
    
    public record CreateWebhookRequest(@NotBlank String url, String events) {}
    public record UpdateWebhookRequest(String url, String events, 
                                       com.nexuspay.domain.entity.WebhookEndpoint.EndpointStatus status) {}
}
