package com.nexuspay.service.provider;

import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.domain.entity.ProviderAccount;
import com.nexuspay.domain.entity.Refund;
import com.nexuspay.service.PaymentIntentService;

import java.math.BigInteger;

/**
 * Application-layer port for payment providers.
 * Implementations should live in infrastructure adapters.
 */
public interface PaymentProvider {

    ProviderAccount.Provider supportedProvider();

    PaymentIntentService.ChargeResult charge(PaymentIntent intent, String paymentMethodId, ProviderAccount account);

    boolean capture(String providerPaymentId, ProviderAccount account);

    boolean cancel(String providerPaymentId, ProviderAccount account);

    default RefundResult refund(String providerPaymentId, BigInteger amount, String currency,
                                Refund.RefundReason reason, ProviderAccount account) {
        throw new UnsupportedOperationException("Refund is not supported for " + supportedProvider());
    }

    default ProviderPaymentStatus fetchPaymentStatus(String providerPaymentId, ProviderAccount account) {
        throw new UnsupportedOperationException("Status fetch is not supported for " + supportedProvider());
    }

    record RefundResult(boolean success, String providerRefundId, String providerResponse,
                        String failureCode, String failureMessage) {}

    record ProviderPaymentStatus(String providerPaymentId, PaymentIntent.PaymentStatus status,
                                 BigInteger amount, String currency, String rawResponse) {}
}
