package com.nexuspay.infra.provider;

import com.nexuspay.common.util.AesGcmEncryptionService;
import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.domain.entity.ProviderAccount;
import com.nexuspay.service.PaymentIntentService;
import com.nexuspay.service.provider.PaymentProvider;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntentCreateParams;
import com.stripe.param.PaymentIntentCancelParams;
import com.stripe.param.PaymentIntentCaptureParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StripeProvider implements PaymentProvider {

    private final AesGcmEncryptionService encryptionService;

    @Override
    public ProviderAccount.Provider supportedProvider() {
        return ProviderAccount.Provider.STRIPE;
    }

    @Override
    public PaymentIntentService.ChargeResult charge(PaymentIntent intent, String paymentMethodId, ProviderAccount account) {
        try {
            Stripe.apiKey = decryptKey(account.getEncryptedSecretKey());

            long amountInCents = intent.getAmount().longValue();
            String currency = intent.getCurrency().toLowerCase();

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(currency)
                    .setPaymentMethod(paymentMethodId)
                    .setCaptureMethod(intent.getCaptureMethod() == PaymentIntent.CaptureMethod.MANUAL
                            ? PaymentIntentCreateParams.CaptureMethod.MANUAL
                            : PaymentIntentCreateParams.CaptureMethod.AUTOMATIC)
                    .setConfirm(true)
                    .build();

            com.stripe.model.PaymentIntent stripeIntent = com.stripe.model.PaymentIntent.create(params);

            String status = stripeIntent.getStatus();
            boolean success = "succeeded".equals(status) || "requires_capture".equals(status);

            return new PaymentIntentService.ChargeResult(
                    success,
                    stripeIntent.getId(),
                    "{\"status\": \"" + status + "\"}",
                    success ? null : "STRIPE_ERROR",
                    success ? null : "Payment " + status
            );

        } catch (StripeException e) {
            log.error("Stripe charge failed: {}", e.getMessage(), e);
            return new PaymentIntentService.ChargeResult(false, null, null, e.getCode(), e.getMessage());
        }
    }

    @Override
    public boolean capture(String providerPaymentId, ProviderAccount account) {
        try {
            Stripe.apiKey = decryptKey(account.getEncryptedSecretKey());

            com.stripe.model.PaymentIntent intent = com.stripe.model.PaymentIntent.retrieve(providerPaymentId);
            intent.capture(PaymentIntentCaptureParams.builder().build());

            return true;
        } catch (StripeException e) {
            log.error("Stripe capture failed: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean cancel(String providerPaymentId, ProviderAccount account) {
        try {
            Stripe.apiKey = decryptKey(account.getEncryptedSecretKey());

            com.stripe.model.PaymentIntent intent = com.stripe.model.PaymentIntent.retrieve(providerPaymentId);
            intent.cancel(PaymentIntentCancelParams.builder().build());

            return true;
        } catch (StripeException e) {
            log.error("Stripe cancel failed: {}", e.getMessage(), e);
            return false;
        }
    }

    private String decryptKey(String encrypted) {
        return encryptionService.decrypt(encrypted);
    }
}