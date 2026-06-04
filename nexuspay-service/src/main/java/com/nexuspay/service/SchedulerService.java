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
    private final RetryService retryService;
    private final OutboxService outboxService;
    private final SubscriptionService subscriptionService;
    private final PayoutService payoutService;
    
    @Scheduled(fixedRate = 60000)
    public void runHealthCheck() {
        log.debug("Running scheduled health check");
        healthMonitorService.checkHealth();
    }
    
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void runReconciliation() {
        log.debug("Generating scheduled payout summaries");
        payoutService.generatePayoutSummaries();
    }
    
    @Scheduled(fixedRate = 60000)
    public void processRetries() {
        log.debug("Processing pending retries");
        retryService.processFailedPayments();
        subscriptionService.processDueRenewals();
    }
    
    @Scheduled(fixedRate = 10000)
    public void processWebhooks() {
        log.debug("Processing pending webhook deliveries");
        outboxService.processOutbox();
    }
}
