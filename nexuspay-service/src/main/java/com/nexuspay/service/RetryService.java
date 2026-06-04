package com.nexuspay.service;

import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.domain.entity.PaymentRequest;
import com.nexuspay.domain.entity.ProviderAccount;
import com.nexuspay.repository.PaymentIntentRepository;
import com.nexuspay.repository.PaymentRequestRepository;
import com.nexuspay.repository.ProviderAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetryService {
    
    private final PaymentIntentRepository paymentIntentRepository;
    private final PaymentRequestRepository paymentRequestRepository;
    private final ProviderAccountRepository providerAccountRepository;
    private final PaymentIntentService paymentIntentService;
    private final DeclineCodeService declineCodeService;
    private final RoutingEngine routingEngine;
    private final ProviderDispatcher providerDispatcher;
    
    @Transactional
    public PaymentIntent executeRetry(UUID paymentIntentId) {
        PaymentIntent intent = paymentIntentRepository.findById(paymentIntentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment intent not found"));
        
        if (intent.getStatus() != PaymentIntent.PaymentStatus.FAILED) {
            log.warn("Payment intent {} is not in FAILED status", paymentIntentId);
            return intent;
        }
        
        // Get last failed request
        List<PaymentRequest> requests = paymentRequestRepository.findByPaymentIntentIdOrderByCreatedAtDesc(paymentIntentId);
        if (requests.isEmpty()) {
            return intent;
        }
        
        PaymentRequest lastRequest = requests.get(0);
        if (lastRequest.getFailureCode() == null) {
            return intent;
        }
        
        DeclineCodeService.RetryStrategy strategy = declineCodeService.getRetryStrategy(
                lastRequest.getFailureCode(), requests.size());
        
        if (!strategy.shouldRetry()) {
            log.info("Retry not recommended for payment intent {}, decline code: {}", 
                    paymentIntentId, lastRequest.getFailureCode());
            return intent;
        }
        
        // Try fallback provider if recommended
        if (strategy.fallbackToNextProvider()) {
            return tryFallbackProvider(intent, lastRequest);
        }
        
        return intent;
    }
    
    private PaymentIntent tryFallbackProvider(PaymentIntent intent, PaymentRequest lastRequest) {
        // Get available providers excluding the failed one
        List<ProviderAccount> accounts = providerAccountRepository
                .findByMerchantIdAndModeAndStatus(intent.getMerchantId(), intent.getMode(), 
                        ProviderAccount.ConnectorStatus.ACTIVE);
        
        Optional<ProviderAccount> fallback = accounts.stream()
                .filter(a -> !a.getId().equals(intent.getConnectorAccountId()))
                .findFirst();
        
        if (fallback.isEmpty()) {
            log.warn("No fallback provider available for merchant {}", intent.getMerchantId());
            return intent;
        }
        
        ProviderAccount fallbackAccount = fallback.get();
        intent.setConnectorAccountId(fallbackAccount.getId());
        intent.setResolvedProvider(PaymentIntent.Provider.valueOf(fallbackAccount.getProvider().name()));
        intent.setStatus(PaymentIntent.PaymentStatus.PROCESSING);
        
        PaymentIntentService.ChargeResult result = providerDispatcher.charge(
                fallbackAccount.getProvider(), intent, lastRequest.getPaymentMethodType());
        
        if (result.success()) {
            intent.setStatus(intent.getCaptureMethod() == PaymentIntent.CaptureMethod.MANUAL 
                    ? PaymentIntent.PaymentStatus.REQUIRES_CAPTURE 
                    : PaymentIntent.PaymentStatus.SUCCEEDED);
            intent.setProviderPaymentId(result.providerPaymentId());
            intent.setProviderResponse(result.providerResponse());
        } else {
            // Create new payment request for failed attempt
            PaymentRequest newRequest = new PaymentRequest();
            newRequest.setPaymentIntentId(intent.getId());
            newRequest.setConnectorAccountId(fallbackAccount.getId());
            newRequest.setAmount(intent.getAmount());
            newRequest.setCurrency(intent.getCurrency());
            newRequest.setPaymentMethodType(lastRequest.getPaymentMethodType());
            newRequest.setStatus(PaymentRequest.RequestStatus.FAILED);
            newRequest.setFailureCode(result.failureCode());
            newRequest.setFailureMessage(result.failureMessage());
            paymentRequestRepository.save(newRequest);
        }
        
        return paymentIntentRepository.save(intent);
    }
    
    public RetryConfig getRetryConfig(UUID merchantId) {
        // Default config - could be stored in database per merchant
        return new RetryConfig(3, 30000L, true);
    }

    @Transactional
    public void processFailedPayments() {
        paymentIntentRepository.findByStatus(PaymentIntent.PaymentStatus.FAILED)
                .forEach(intent -> {
                    try {
                        executeRetry(intent.getId());
                    } catch (Exception e) {
                        log.warn("Retry failed for payment intent {}: {}", intent.getId(), e.getMessage());
                    }
                });
    }
    
    public record RetryConfig(int maxRetries, long retryDelayMs, boolean enableFallback) {}
}
