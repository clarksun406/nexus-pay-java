package com.nexuspay.service;

import org.junit.jupiter.api.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class HealthMonitorServiceTest {
    
    private final HealthMonitorService service = new HealthMonitorService(null, null);
    
    @Test
    void shouldRecordSuccess() {
        UUID accountId = UUID.randomUUID();
        service.recordRequest(accountId, true);
        
        var metrics = service.getMetrics(accountId);
        assertEquals(1, metrics.getTotalRequests());
        assertEquals(1, metrics.getSuccessRequests());
        assertEquals(0, metrics.getConsecutiveFailures());
    }
    
    @Test
    void shouldRecordFailure() {
        UUID accountId = UUID.randomUUID();
        service.recordRequest(accountId, false);
        
        var metrics = service.getMetrics(accountId);
        assertEquals(1, metrics.getTotalRequests());
        assertEquals(1, metrics.getFailedRequests());
        assertEquals(1, metrics.getConsecutiveFailures());
    }
    
    @Test
    void shouldCalculateSuccessRate() {
        UUID accountId = UUID.randomUUID();
        service.recordRequest(accountId, true);
        service.recordRequest(accountId, true);
        service.recordRequest(accountId, false);
        
        var metrics = service.getMetrics(accountId);
        assertEquals(66.66, metrics.getSuccessRate(), 0.1);
    }
}
