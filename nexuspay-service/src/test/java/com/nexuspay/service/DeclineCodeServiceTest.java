package com.nexuspay.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DeclineCodeServiceTest {
    
    private final DeclineCodeService service = new DeclineCodeService();
    
    @Test
    void shouldReturnTemporaryForProcessingError() {
        assertEquals(DeclineCodeService.DeclineCategory.TEMPORARY, service.getCategory("processing_error"));
    }
    
    @Test
    void shouldReturnPermanentForExpiredCard() {
        assertEquals(DeclineCodeService.DeclineCategory.PERMANENT, service.getCategory("expired_card"));
    }
    
    @Test
    void shouldReturnUnknownForUnknownCode() {
        assertEquals(DeclineCodeService.DeclineCategory.UNKNOWN, service.getCategory("unknown_code"));
    }
    
    @Test
    void shouldRetryTemporaryErrors() {
        assertTrue(service.isRetryable("processing_error"));
    }
    
    @Test
    void shouldNotRetryPermanentErrors() {
        assertFalse(service.isRetryable("expired_card"));
    }
    
    @Test
    void shouldCalculateRetryStrategy() {
        var strategy = service.getRetryStrategy("processing_error", 1);
        assertTrue(strategy.shouldRetry());
        assertTrue(strategy.fallbackToNextProvider());
    }
}
