package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.domain.entity.Dispute;
import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.repository.DisputeRepository;
import com.nexuspay.repository.PaymentIntentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DisputeService {
    
    private final DisputeRepository disputeRepository;
    private final PaymentIntentRepository paymentIntentRepository;
    
    public List<Dispute> listDisputes(UUID merchantId) {
        return disputeRepository.findByMerchantId(merchantId);
    }
    
    public Dispute getDispute(UUID merchantId, UUID disputeId) {
        return disputeRepository.findByMerchantIdAndId(merchantId, disputeId)
                .orElseThrow(() -> new BusinessException("Dispute not found", HttpStatus.NOT_FOUND));
    }
    
    @Transactional
    public Dispute saveEvidence(UUID merchantId, UUID disputeId, String evidence) {
        Dispute dispute = getDispute(merchantId, disputeId);
        
        if (dispute.getStatus() == Dispute.DisputeStatus.WON || 
            dispute.getStatus() == Dispute.DisputeStatus.LOST) {
            throw new BusinessException("Cannot update closed dispute", HttpStatus.BAD_REQUEST);
        }
        
        dispute.setEvidence(evidence);
        return disputeRepository.save(dispute);
    }
    
    @Transactional
    public Dispute submitEvidence(UUID merchantId, UUID disputeId) {
        Dispute dispute = getDispute(merchantId, disputeId);
        
        if (dispute.getEvidence() == null || dispute.getEvidence().isBlank()) {
            throw new BusinessException("No evidence to submit", HttpStatus.BAD_REQUEST);
        }
        
        // In production, submit to provider API (Stripe)
        dispute.setStatus(Dispute.DisputeStatus.UNDER_REVIEW);
        dispute.setEvidenceSubmittedAt(java.time.Instant.now());
        
        return disputeRepository.save(dispute);
    }
    
    @Transactional
    public Dispute upsertFromWebhook(UUID merchantId, String providerDisputeId, 
                                     UUID paymentIntentId, Dispute.DisputeStatus status,
                                     Dispute.DisputeReason reason, java.math.BigInteger amount,
                                     String currency) {
        return disputeRepository.findByProviderDisputeId(providerDisputeId)
                .map(existing -> {
                    existing.setStatus(status);
                    return disputeRepository.save(existing);
                })
                .orElseGet(() -> {
                    Dispute dispute = new Dispute();
                    dispute.setMerchantId(merchantId);
                    dispute.setProviderDisputeId(providerDisputeId);
                    dispute.setPaymentIntentId(paymentIntentId);
                    dispute.setStatus(status);
                    dispute.setReason(reason);
                    dispute.setAmount(amount);
                    dispute.setCurrency(currency);
                    return disputeRepository.save(dispute);
                });
    }
}
