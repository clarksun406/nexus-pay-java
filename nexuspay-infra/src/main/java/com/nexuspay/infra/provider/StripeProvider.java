package com.nexuspay.infra.provider;

import com.nexuspay.common.util.AesGcmEncryptionService;
import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.domain.entity.ProviderAccount;
import com.nexuspay.domain.entity.Refund;
import com.nexuspay.service.PaymentIntentService;
import com.nexuspay.service.provider.PaymentProvider;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.param.PaymentIntentCancelParams;
import com.stripe.param.PaymentIntentCaptureParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

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

    @Override
    public RefundResult refund(String providerPaymentId, BigInteger amount, String currency,
                               Refund.RefundReason reason, ProviderAccount account) {
        try {
            Stripe.apiKey = decryptKey(account.getEncryptedSecretKey());

            RefundCreateParams.Builder builder = RefundCreateParams.builder()
                    .setPaymentIntent(providerPaymentId)
                    .setAmount(amount.longValue());

            if (reason != null) {
                switch (reason) {
                    case FRAUDULENT -> builder.setReason(RefundCreateParams.Reason.FRAUDULENT);
                    case DUPLICATE -> builder.setReason(RefundCreateParams.Reason.DUPLICATE);
                    case REQUESTED_BY_CUSTOMER -> builder.setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER);
                    default -> {
                    }
                }
            }

            com.stripe.model.Refund refund = com.stripe.model.Refund.create(builder.build());
            boolean success = "succeeded".equals(refund.getStatus()) || "pending".equals(refund.getStatus());
            return new RefundResult(
                    success,
                    refund.getId(),
                    "{\"status\": \"" + refund.getStatus() + "\"}",
                    success ? null : "STRIPE_REFUND_ERROR",
                    success ? null : "Refund " + refund.getStatus()
            );
        } catch (StripeException e) {
            log.error("Stripe refund failed: {}", e.getMessage(), e);
            return new RefundResult(false, null, null, e.getCode(), e.getMessage());
        }
    }

    @Override
    public ProviderPaymentStatus fetchPaymentStatus(String providerPaymentId, ProviderAccount account) {
        try {
            Stripe.apiKey = decryptKey(account.getEncryptedSecretKey());
            com.stripe.model.PaymentIntent stripeIntent = com.stripe.model.PaymentIntent.retrieve(providerPaymentId);
            return new ProviderPaymentStatus(
                    stripeIntent.getId(),
                    toPaymentStatus(stripeIntent.getStatus()),
                    BigInteger.valueOf(stripeIntent.getAmount()),
                    stripeIntent.getCurrency(),
                    "{\"status\": \"" + stripeIntent.getStatus() + "\"}"
            );
        } catch (StripeException e) {
            log.error("Stripe status fetch failed: {}", e.getMessage(), e);
            return new ProviderPaymentStatus(providerPaymentId, PaymentIntent.PaymentStatus.FAILED, null, null,
                    "{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private String decryptKey(String encrypted) {
        return encryptionService.decrypt(encrypted);
    }

    private PaymentIntent.PaymentStatus toPaymentStatus(String status) {
        if ("succeeded".equals(status)) return PaymentIntent.PaymentStatus.SUCCEEDED;
        if ("requires_capture".equals(status)) return PaymentIntent.PaymentStatus.REQUIRES_CAPTURE;
        if ("requires_action".equals(status)) return PaymentIntent.PaymentStatus.REQUIRES_ACTION;
        if ("canceled".equals(status)) return PaymentIntent.PaymentStatus.CANCELED;
        if ("processing".equals(status)) return PaymentIntent.PaymentStatus.PROCESSING;
        if ("requires_payment_method".equals(status)) return PaymentIntent.PaymentStatus.REQUIRES_PAYMENT_METHOD;
        return PaymentIntent.PaymentStatus.FAILED;
    }
}
