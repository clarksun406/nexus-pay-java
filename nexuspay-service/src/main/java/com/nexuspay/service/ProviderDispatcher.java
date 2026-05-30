package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.domain.entity.ProviderAccount;
import com.nexuspay.repository.ProviderAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProviderDispatcher {
    
    private final ProviderAccountRepository providerAccountRepository;
    
    public PaymentIntentService.ChargeResult charge(ProviderAccount.Provider provider, PaymentIntent intent, String paymentMethodId) {
        ProviderAccount account = providerAccountRepository.findById(intent.getConnectorAccountId())
                .orElseThrow(() -> new BusinessException("Provider account not found", HttpStatus.NOT_FOUND));
        
        return switch (provider) {
            case STRIPE -> stripeCharge(intent, paymentMethodId, account);
            case SQUARE -> squareCharge(intent, paymentMethodId, account);
            case BRAINTREE -> braintreeCharge(intent, paymentMethodId, account);
        };
    }
    
    public boolean capture(ProviderAccount.Provider provider, String providerPaymentId, UUID accountId) {
        return switch (provider) {
            case STRIPE -> stripeCapture(providerPaymentId);
            case SQUARE, BRAINTREE -> true;
        };
    }
    
    public boolean cancel(ProviderAccount.Provider provider, String providerPaymentId, UUID accountId) {
        return switch (provider) {
            case STRIPE -> stripeCancel(providerPaymentId);
            case SQUARE, BRAINTREE -> true;
        };
    }
    
    private PaymentIntentService.ChargeResult stripeCharge(PaymentIntent intent, String paymentMethodId, ProviderAccount account) {
        // Mock implementation - in production, call Stripe API
        return new PaymentIntentService.ChargeResult(
                true, 
                "pi_" + UUID.randomUUID().toString().replace("-", ""),
                "{\"status\": \"succeeded\"}",
                null, null
        );
    }
    
    private PaymentIntentService.ChargeResult squareCharge(PaymentIntent intent, String paymentMethodId, ProviderAccount account) {
        return new PaymentIntentService.ChargeResult(
                true,
                "sq_" + UUID.randomUUID().toString().replace("-", ""),
                "{\"status\": \"COMPLETED\"}",
                null, null
        );
    }
    
    private PaymentIntentService.ChargeResult braintreeCharge(PaymentIntent intent, String paymentMethodId, ProviderAccount account) {
        return new PaymentIntentService.ChargeResult(
                true,
                "bt_" + UUID.randomUUID().toString().replace("-", ""),
                "{\"status\": \"authorized\"}",
                null, null
        );
    }
    
    private boolean stripeCapture(String providerPaymentId) {
        return true;
    }
    
    private boolean stripeCancel(String providerPaymentId) {
        return true;
    }
}
