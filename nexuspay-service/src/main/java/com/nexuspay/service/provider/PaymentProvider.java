package com.nexuspay.service.provider;

import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.domain.entity.ProviderAccount;
import com.nexuspay.service.PaymentIntentService;

/**
 * Application-layer port for payment providers.
 * Implementations should live in infrastructure adapters.
 */
public interface PaymentProvider {

    ProviderAccount.Provider supportedProvider();

    PaymentIntentService.ChargeResult charge(PaymentIntent intent, String paymentMethodId, ProviderAccount account);

    boolean capture(String providerPaymentId, ProviderAccount account);

    boolean cancel(String providerPaymentId, ProviderAccount account);
}