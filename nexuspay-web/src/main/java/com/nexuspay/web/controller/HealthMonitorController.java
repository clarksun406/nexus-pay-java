package com.nexuspay.web.controller;

import com.nexuspay.service.HealthMonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
public class HealthMonitorController {
    
    private final HealthMonitorService healthMonitorService;
    
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(@RequestAttribute("merchantId") UUID merchantId) {
        return ResponseEntity.ok(healthMonitorService.getDashboard(merchantId));
    }
    
    @GetMapping("/connectors/{accountId}")
    public ResponseEntity<?> getConnectorHealth(
            @RequestAttribute("merchantId") UUID merchantId,
            @PathVariable UUID accountId) {
        return ResponseEntity.ok(Map.of(
                "accountId", accountId,
                "metrics", healthMonitorService.getMetrics(merchantId, accountId)
        ));
    }
    
    @GetMapping("/unhealthy")
    public ResponseEntity<?> getUnhealthyConnectors(@RequestAttribute("merchantId") UUID merchantId) {
        return ResponseEntity.ok(Map.of(
                "unhealthyConnectors", healthMonitorService.getUnhealthyConnectors(merchantId, 80.0)
        ));
    }
}
