package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.domain.entity.ProviderAccount;
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
    private final ProviderDispatcher providerDispatcher;
    
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
        if (intent.getProviderPaymentId() == null || intent.getResolvedProvider() == null
                || intent.getConnectorAccountId() == null) {
            return null;
        }

        var actual = providerDispatcher.fetchPaymentStatus(
                ProviderAccount.Provider.valueOf(intent.getResolvedProvider().name()),
                intent.getProviderPaymentId(),
                intent.getConnectorAccountId()
        );

        if (actual == null) {
            return null;
        }

        boolean statusMatches = actual.status() == intent.getStatus();
        boolean amountMatches = actual.amount() == null || actual.amount().equals(intent.getAmount());
        boolean currencyMatches = actual.currency() == null
                || actual.currency().equalsIgnoreCase(intent.getCurrency());

        if (statusMatches && amountMatches && currencyMatches) {
            return null;
        }

        Discrepancy discrepancy = new Discrepancy();
        discrepancy.setPaymentIntentId(intent.getId());
        discrepancy.setProviderPaymentId(intent.getProviderPaymentId());
        discrepancy.setExpectedAmount(intent.getAmount());
        discrepancy.setActualAmount(actual.amount());
        discrepancy.setExpectedStatus(intent.getStatus().name());
        discrepancy.setActualStatus(actual.status() != null ? actual.status().name() : null);
        discrepancy.setReason("Local payment intent does not match provider state");
        discrepancy.setDetectedAt(Instant.now());
        return discrepancy;
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
        return runReconciliation(merchantId, Instant.now().minusSeconds(86400), Instant.now()).getDiscrepancies();
    }
}
