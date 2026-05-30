package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.repository.PaymentIntentRepository;
import lombok.Data;
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
public class ReconciliationService {
    
    private final PaymentIntentRepository paymentIntentRepository;
    
    @Data
    public static class ReconciliationReport {
        private UUID merchantId;
        private Instant startTime;
        private Instant endTime;
        private int totalIntents;
        private int matchedIntents;
        private int openDiscrepancies;
        private List<Discrepancy> discrepancies = new ArrayList<>();
    }
    
    @Data
    public static class Discrepancy {
        private UUID paymentIntentId;
        private String providerPaymentId;
        private BigInteger expectedAmount;
        private BigInteger actualAmount;
        private String expectedStatus;
        private String actualStatus;
        private String reason;
        private Instant detectedAt;
        private String resolution;
    }
    
    public ReconciliationReport runReconciliation(UUID merchantId, Instant startTime, Instant endTime) {
        ReconciliationReport report = new ReconciliationReport();
        report.setMerchantId(merchantId);
        report.setStartTime(startTime);
        report.setEndTime(endTime);
        
        List<PaymentIntent> intents = paymentIntentRepository.findByMerchantId(merchantId);
        
        for (PaymentIntent intent : intents) {
            report.setTotalIntents(report.getTotalIntents() + 1);
            
            Discrepancy discrepancy = checkDiscrepancy(intent);
            if (discrepancy != null) {
                report.getDiscrepancies().add(discrepancy);
                report.setOpenDiscrepancies(report.getOpenDiscrepancies() + 1);
            } else {
                report.setMatchedIntents(report.getMatchedIntents() + 1);
            }
        }
        
        return report;
    }
    
    private Discrepancy checkDiscrepancy(PaymentIntent intent) {
        // In production, fetch actual state from provider API and compare
        // For now, return null (no discrepancy)
        
        if (intent.getProviderPaymentId() == null) {
            return null;
        }
        
        // Mock: check if provider payment exists
        // Real implementation would call provider API
        
        return null;
    }
    
    @Transactional
    public void resolveDiscrepancy(UUID paymentIntentId, String resolution) {
        PaymentIntent intent = paymentIntentRepository.findById(paymentIntentId)
                .orElseThrow(() -> new BusinessException("Payment intent not found", 
                        org.springframework.http.HttpStatus.NOT_FOUND));
        
        // Apply resolution logic
        log.info("Discrepancy resolved for payment intent {}: {}", paymentIntentId, resolution);
    }
    
    public List<Discrepancy> getOpenDiscrepancies(UUID merchantId) {
        // In production, query from a discrepancies table
        return Collections.emptyList();
    }
}
