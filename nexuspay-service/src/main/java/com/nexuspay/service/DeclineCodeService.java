package com.nexuspay.service;

import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.domain.entity.PaymentRequest;
import com.nexuspay.domain.entity.ProviderAccount;
import com.nexuspay.repository.PaymentIntentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeclineCodeService {
    
    private final Map<String, DeclineCategory> declineCategories = Map.of(
            "insufficient_funds", DeclineCategory.SFUNDS,
            "card_declined", DeclineCategory.GENERIC,
            "expired_card", DeclineCategory.PERMANENT,
            "incorrect_cvc", DeclineCategory.PERMANENT,
            "processing_error", DeclineCategory.TEMPORARY,
            "rate_limit", DeclineCategory.TEMPORARY,
            "authentication_required", DeclineCategory.AUTH,
            "do_not_honor", DeclineCategory.GENERIC,
            "invalid_card_number", DeclineCategory.PERMANENT
    );
    
    public DeclineCategory getCategory(String code) {
        return declineCategories.getOrDefault(code.toLowerCase(), DeclineCategory.UNKNOWN);
    }
    
    public boolean isRetryable(String code) {
        DeclineCategory category = getCategory(code);
        return category == DeclineCategory.TEMPORARY || category == DeclineCategory.AUTH;
    }
    
    public RetryStrategy getRetryStrategy(String code, int attemptCount) {
        DeclineCategory category = getCategory(code);
        
        return switch (category) {
            case TEMPORARY -> new RetryStrategy(true, calculateDelay(attemptCount), attemptCount < 3);
            case AUTH -> new RetryStrategy(true, 0, false);
            case SFUNDS -> new RetryStrategy(false, 0, false);
            case PERMANENT -> new RetryStrategy(false, 0, false);
            default -> new RetryStrategy(false, 0, false);
        };
    }
    
    private long calculateDelay(int attempt) {
        // Exponential backoff: 1s, 2s, 4s, 8s...
        return (long) (Math.pow(2, attempt) * 1000);
    }
    
    public enum DeclineCategory {
        TEMPORARY,    // Temporary issue, retry recommended
        PERMANENT,    // Permanent failure, do not retry
        SFUNDS,       // Insufficient funds
        AUTH,         // Authentication required (3DS)
        GENERIC,      // Generic decline
        UNKNOWN       // Unknown reason
    }
    
    public record RetryStrategy(boolean shouldRetry, long delayMs, boolean fallbackToNextProvider) {}
    
    public Map<String, Object> getAllCategories() {
        Map<String, Object> result = new HashMap<>();
        for (DeclineCategory cat : DeclineCategory.values()) {
            result.put(cat.name(), Map.of(
                    "isRetryable", cat == TEMPORARY || cat == AUTH,
                    "description", cat.name().toLowerCase()
            ));
        }
        return result;
    }
}
