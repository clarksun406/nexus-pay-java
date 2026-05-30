package com.nexuspay.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {
    
    private final HealthMonitorService healthMonitorService;
    
    @Scheduled(fixedRate = 60000)
    public void runHealthCheck() {
        log.debug("Running scheduled health check");
        healthMonitorService.checkHealth();
    }
    
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void runReconciliation() {
        log.debug("Running scheduled reconciliation");
        // Run reconciliation for all merchants
    }
    
    @Scheduled(fixedRate = 60000)
    public void processRetries() {
        log.debug("Processing pending retries");
        // Process payment retries with exponential backoff
    }
    
    @Scheduled(fixedRate = 10000)
    public void processWebhooks() {
        log.debug("Processing pending webhook deliveries");
        // Deliver pending webhooks
    }
}
