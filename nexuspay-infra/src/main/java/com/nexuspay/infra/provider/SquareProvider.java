package com.nexuspay.infra.provider;

import com.nexuspay.common.util.AesGcmEncryptionService;
import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.domain.entity.ProviderAccount;
import com.nexuspay.service.PaymentIntentService;
import com.nexuspay.service.provider.PaymentProvider;
import com.squareup.square.SquareClient;
import com.squareup.square.api.PaymentsApi;
import com.squareup.square.models.CreatePaymentRequest;
import com.squareup.square.models.Money;
import com.squareup.square.models.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SquareProvider implements PaymentProvider {

    private final AesGcmEncryptionService encryptionService;

    @Override
    public ProviderAccount.Provider supportedProvider() {
        return ProviderAccount.Provider.SQUARE;
    }

    @Override
    public PaymentIntentService.ChargeResult charge(PaymentIntent intent, String paymentMethodId, ProviderAccount account) {
        try {
            SquareClient client = new SquareClient.Builder()
                    .accessToken(decryptKey(account.getEncryptedSecretKey()))
                    .environment(com.squareup.square.Environment.SANDBOX)
                    .build();

            PaymentsApi paymentsApi = client.getPaymentsApi();

            Money amountMoney = new Money.Builder()
                    .amount(intent.getAmount().longValue())
                    .currency(intent.getCurrency().toUpperCase())
                    .build();

            String idempotencyKey = UUID.randomUUID().toString();

            CreatePaymentRequest request = new CreatePaymentRequest.Builder(
                    paymentMethodId,
                    idempotencyKey,
                    amountMoney
            ).build();

            var response = paymentsApi.createPayment(request);
            Payment payment = response.getPayment();

            String status = payment.getStatus();
            boolean success = "COMPLETED".equals(status);

            return new PaymentIntentService.ChargeResult(
                    success,
                    payment.getId(),
                    "{\"status\": \"" + status + "\"}",
                    success ? null : "SQUARE_ERROR",
                    success ? null : "Payment " + status
            );

        } catch (Exception e) {
            log.error("Square charge failed: {}", e.getMessage(), e);
            return new PaymentIntentService.ChargeResult(false, null, null, "SQUARE_ERROR", e.getMessage());
        }
    }

    @Override
    public boolean capture(String providerPaymentId, ProviderAccount account) {
        // Square captures automatically for most cases
        return true;
    }

    @Override
    public boolean cancel(String providerPaymentId, ProviderAccount account) {
        try {
            SquareClient client = new SquareClient.Builder()
                    .accessToken(decryptKey(account.getEncryptedSecretKey()))
                    .environment(com.squareup.square.Environment.SANDBOX)
                    .build();

            client.getPaymentsApi().cancelPayment(providerPaymentId);
            return true;
        } catch (Exception e) {
            log.error("Square cancel failed: {}", e.getMessage(), e);
            return false;
        }
    }

    private String decryptKey(String encrypted) {
        return encryptionService.decrypt(encrypted);
    }
}