package com.nexuspay.web.controller;

import com.nexuspay.service.ConnectorService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/connectors")
@RequiredArgsConstructor
public class ConnectorController {
    
    private final ConnectorService connectorService;
    
    @PostMapping
    public ResponseEntity<?> create(
            @RequestAttribute("merchantId") UUID merchantId,
            @Valid @RequestBody CreateConnectorRequest req) {
        return ResponseEntity.ok(connectorService.create(merchantId,
                new ConnectorService.CreateConnectorRequest(
                        req.provider(), req.mode(), req.label(), req.encryptedSecretKey(),
                        req.encryptedPublishableKey(), req.weight(), req.isPrimary())));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable UUID id,
            @RequestBody UpdateConnectorRequest req) {
        return ResponseEntity.ok(connectorService.update(id,
                new ConnectorService.UpdateConnectorRequest(
                        req.label(), req.weight(), req.isPrimary(), req.status())));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable UUID id) {
        return ResponseEntity.ok(connectorService.getConnector(id));
    }
    
    @GetMapping
    public ResponseEntity<?> list(@RequestAttribute("merchantId") UUID merchantId) {
        return ResponseEntity.ok(connectorService.listConnectors(merchantId));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        connectorService.delete(id);
        return ResponseEntity.ok().build();
    }
    
    public record CreateConnectorRequest(
            @NotNull com.nexuspay.domain.entity.ProviderAccount.Provider provider,
            com.nexuspay.domain.entity.ProviderAccount.Mode mode,
            @NotBlank String label,
            String encryptedSecretKey,
            String encryptedPublishableKey,
            Integer weight,
            Boolean isPrimary) {}
    
    public record UpdateConnectorRequest(
            String label,
            Integer weight,
            Boolean isPrimary,
            com.nexuspay.domain.entity.ProviderAccount.ConnectorStatus status) {}
}
