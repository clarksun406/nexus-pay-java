package com.nexuspay.service.provider;

import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.domain.entity.ProviderAccount;
import com.nexuspay.service.PaymentIntentService;

public interface PaymentProvider {
    
    PaymentIntentService.ChargeResult charge(PaymentIntent intent, String paymentMethodId, ProviderAccount account);
    
    boolean capture(String providerPaymentId, ProviderAccount account);
    
    boolean cancel(String providerPaymentId, ProviderAccount account);
}
