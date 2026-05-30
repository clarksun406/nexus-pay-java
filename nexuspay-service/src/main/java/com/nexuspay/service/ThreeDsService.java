package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.repository.PaymentIntentRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ThreeDsService {
    
    private final PaymentIntentRepository paymentIntentRepository;
    
    // In-memory challenge storage (should use Redis in production)
    private final Map<String, ThreeDsChallenge> activeChallenges = new HashMap<>();
    
    @Data
    public static class ThreeDsChallenge {
        private String id;
        private UUID paymentIntentId;
        private String status;
        private String acsUrl;
        private String creq;
        private Instant createdAt;
        private Instant expiresAt;
    }
    
    @Transactional
    public ThreeDsChallenge createChallenge(UUID paymentIntentId, String acsUrl, String creq) {
        String challengeId = UUID.randomUUID().toString().replace("-", "");
        
        ThreeDsChallenge challenge = new ThreeDsChallenge();
        challenge.setId(challengeId);
        challenge.setPaymentIntentId(paymentIntentId);
        challenge.setStatus("PENDING");
        challenge.setAcsUrl(acsUrl);
        challenge.setCreq(creq);
        challenge.setCreatedAt(Instant.now());
        challenge.setExpiresAt(Instant.now().plusSeconds(300)); // 5 minutes
        
        activeChallenges.put(challengeId, challenge);
        
        PaymentIntent intent = paymentIntentRepository.findById(paymentIntentId)
                .orElseThrow(() -> new BusinessException("Payment intent not found", HttpStatus.NOT_FOUND));
        intent.setStatus(PaymentIntent.PaymentStatus.REQUIRES_ACTION);
        intent.setThreeDsActionUrl("/pub/3ds/challenge/" + challengeId);
        paymentIntentRepository.save(intent);
        
        return challenge;
    }
    
    public ThreeDsChallenge getChallenge(String challengeId) {
        ThreeDsChallenge challenge = activeChallenges.get(challengeId);
        if (challenge == null) {
            throw new BusinessException("Challenge not found or expired", HttpStatus.NOT_FOUND);
        }
        
        if (challenge.getExpiresAt().isBefore(Instant.now())) {
            activeChallenges.remove(challengeId);
            throw new BusinessException("Challenge expired", HttpStatus.BAD_REQUEST);
        }
        
        return challenge;
    }
    
    @Transactional
    public void completeChallenge(String challengeId, String cres) {
        ThreeDsChallenge challenge = getChallenge(challengeId);
        
        // In production, validate the cres (challenge response) with the provider
        challenge.setStatus("COMPLETED");
        
        PaymentIntent intent = paymentIntentRepository.findById(challenge.getPaymentIntentId())
                .orElseThrow();
        intent.setStatus(PaymentIntent.PaymentStatus.PROCESSING);
        paymentIntentRepository.save(intent);
        
        activeChallenges.remove(challengeId);
    }
    
    @Transactional
    public void failChallenge(String challengeId, String reason) {
        ThreeDsChallenge challenge = activeChallenges.remove(challengeId);
        
        if (challenge != null) {
            PaymentIntent intent = paymentIntentRepository.findById(challenge.getPaymentIntentId())
                    .orElseThrow();
            intent.setStatus(PaymentIntent.PaymentStatus.FAILED);
            paymentIntentRepository.save(intent);
        }
    }
    
    public boolean isRequired(String providerResponse) {
        // Check if 3DS is required based on provider response
        return providerResponse != null && 
                (providerResponse.contains("3DS") || providerResponse.contains("authentication_required"));
    }
}
