package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.domain.entity.ProviderAccount;
import com.nexuspay.repository.PaymentIntentRepository;
import com.nexuspay.repository.ProviderAccountRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthMonitorService {
    
    private final ProviderAccountRepository providerAccountRepository;
    private final PaymentIntentRepository paymentIntentRepository;
    
    // In-memory health metrics (could be moved to Redis)
    private final Map<UUID, HealthMetrics> metricsMap = new ConcurrentHashMap<>();
    
    @Data
    public static class HealthMetrics {
        private int totalRequests = 0;
        private int successRequests = 0;
        private int failedRequests = 0;
        private Instant lastSuccess;
        private Instant lastFailure;
        private double successRate = 100.0;
        private int consecutiveFailures = 0;
    }
    
    public void recordRequest(UUID accountId, boolean success) {
        HealthMetrics metrics = metricsMap.computeIfAbsent(accountId, k -> new HealthMetrics());
        
        metrics.setTotalRequests(metrics.getTotalRequests() + 1);
        
        if (success) {
            metrics.setSuccessRequests(metrics.getSuccessRequests() + 1);
            metrics.setLastSuccess(Instant.now());
            metrics.setConsecutiveFailures(0);
        } else {
            metrics.setFailedRequests(metrics.getFailedRequests() + 1);
            metrics.setLastFailure(Instant.now());
            metrics.setConsecutiveFailures(metrics.getConsecutiveFailures() + 1);
        }
        
        if (metrics.getTotalRequests() > 0) {
            metrics.setSuccessRate((double) metrics.getSuccessRequests() / metrics.getTotalRequests() * 100);
        }
    }
    
    public HealthMetrics getMetrics(UUID accountId) {
        return metricsMap.getOrDefault(accountId, new HealthMetrics());
    }

    public HealthMetrics getMetrics(UUID merchantId, UUID accountId) {
        providerAccountRepository.findByMerchantIdAndId(merchantId, accountId)
                .orElseThrow(() -> new BusinessException("Connector not found", HttpStatus.NOT_FOUND));
        return getMetrics(accountId);
    }
    
    public List<UUID> getUnhealthyConnectors(UUID merchantId, double threshold) {
        List<ProviderAccount> accounts = providerAccountRepository.findByMerchantId(merchantId);
        List<UUID> unhealthy = new ArrayList<>();
        
        for (ProviderAccount account : accounts) {
            HealthMetrics metrics = metricsMap.get(account.getId());
            if (metrics != null && metrics.getSuccessRate() < threshold) {
                unhealthy.add(account.getId());
            }
        }
        
        return unhealthy;
    }
    
    @Scheduled(fixedRate = 60000)
    public void checkHealth() {
        log.debug("Running health check for all connectors");
        
        for (Map.Entry<UUID, HealthMetrics> entry : metricsMap.entrySet()) {
            HealthMetrics metrics = entry.getValue();
            
            // Auto-demote connectors with consecutive failures
            if (metrics.getConsecutiveFailures() >= 5) {
                demoteConnector(entry.getKey());
            }
            
            // Restore connectors that recovered
            if (metrics.getSuccessRate() > 90 && metrics.getLastSuccess() != null) {
                Instant fiveMinutesAgo = Instant.now().minus(5, ChronoUnit.MINUTES);
                if (metrics.getLastSuccess().isAfter(fiveMinutesAgo)) {
                    restoreConnector(entry.getKey());
                }
            }
        }
    }
    
    private void demoteConnector(UUID accountId) {
        providerAccountRepository.findById(accountId).ifPresent(account -> {
            if (account.getStatus() == ProviderAccount.ConnectorStatus.ACTIVE) {
                account.setStatus(ProviderAccount.ConnectorStatus.UNHEALTHY);
                providerAccountRepository.save(account);
                log.warn("Connector {} demoted to UNHEALTHY status", account.getLabel());
            }
        });
    }
    
    private void restoreConnector(UUID accountId) {
        providerAccountRepository.findById(accountId).ifPresent(account -> {
            if (account.getStatus() == ProviderAccount.ConnectorStatus.UNHEALTHY) {
                account.setStatus(ProviderAccount.ConnectorStatus.ACTIVE);
                providerAccountRepository.save(account);
                log.info("Connector {} restored to ACTIVE status", account.getLabel());
            }
        });
    }
    
    public Map<String, Object> getDashboard(UUID merchantId) {
        Map<String, Object> dashboard = new HashMap<>();
        
        List<ProviderAccount> accounts = providerAccountRepository.findByMerchantId(merchantId);
        List<Map<String, Object>> connectorHealth = new ArrayList<>();
        
        for (ProviderAccount account : accounts) {
            Map<String, Object> health = new HashMap<>();
            health.put("id", account.getId());
            health.put("label", account.getLabel());
            health.put("provider", account.getProvider());
            health.put("status", account.getStatus());
            health.put("metrics", getMetrics(account.getId()));
            connectorHealth.add(health);
        }
        
        dashboard.put("connectors", connectorHealth);
        dashboard.put("timestamp", Instant.now());
        
        return dashboard;
    }
}
