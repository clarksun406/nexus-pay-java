package com.nexuspay.infra.provider;

import com.nexuspay.common.util.AesGcmEncryptionService;
import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.domain.entity.ProviderAccount;
import com.nexuspay.domain.entity.Refund;
import com.nexuspay.service.PaymentIntentService;
import com.nexuspay.service.provider.PaymentProvider;
import com.squareup.square.SquareClient;
import com.squareup.square.api.PaymentsApi;
import com.squareup.square.models.CreatePaymentRequest;
import com.squareup.square.models.RefundPaymentRequest;
import com.squareup.square.models.Money;
import com.squareup.square.models.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
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
                    idempotencyKey
            )
                    .amountMoney(amountMoney)
                    .build();

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

    @Override
    public RefundResult refund(String providerPaymentId, BigInteger amount, String currency,
                               Refund.RefundReason reason, ProviderAccount account) {
        try {
            SquareClient client = createClient(account);

            Money amountMoney = new Money.Builder()
                    .amount(amount.longValue())
                    .currency(currency.toUpperCase())
                    .build();

            RefundPaymentRequest request = new RefundPaymentRequest.Builder(
                    UUID.randomUUID().toString(),
                    amountMoney
            )
                    .paymentId(providerPaymentId)
                    .reason(reason != null ? reason.name() : null)
                    .build();

            var response = client.getRefundsApi().refundPayment(request);
            var refund = response.getRefund();
            String status = refund.getStatus();
            boolean success = "COMPLETED".equals(status) || "PENDING".equals(status);

            return new RefundResult(
                    success,
                    refund.getId(),
                    "{\"status\": \"" + status + "\"}",
                    success ? null : "SQUARE_REFUND_ERROR",
                    success ? null : "Refund " + status
            );
        } catch (Exception e) {
            log.error("Square refund failed: {}", e.getMessage(), e);
            return new RefundResult(false, null, null, "SQUARE_REFUND_ERROR", e.getMessage());
        }
    }

    @Override
    public ProviderPaymentStatus fetchPaymentStatus(String providerPaymentId, ProviderAccount account) {
        try {
            SquareClient client = createClient(account);
            Payment payment = client.getPaymentsApi().getPayment(providerPaymentId).getPayment();
            return new ProviderPaymentStatus(
                    payment.getId(),
                    toPaymentStatus(payment.getStatus()),
                    payment.getAmountMoney() != null ? BigInteger.valueOf(payment.getAmountMoney().getAmount()) : null,
                    payment.getAmountMoney() != null ? payment.getAmountMoney().getCurrency() : null,
                    "{\"status\": \"" + payment.getStatus() + "\"}"
            );
        } catch (Exception e) {
            log.error("Square status fetch failed: {}", e.getMessage(), e);
            return new ProviderPaymentStatus(providerPaymentId, PaymentIntent.PaymentStatus.FAILED, null, null,
                    "{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private String decryptKey(String encrypted) {
        return encryptionService.decrypt(encrypted);
    }

    private SquareClient createClient(ProviderAccount account) {
        return new SquareClient.Builder()
                .accessToken(decryptKey(account.getEncryptedSecretKey()))
                .environment(account.getMode() == ProviderAccount.Mode.LIVE
                        ? com.squareup.square.Environment.PRODUCTION
                        : com.squareup.square.Environment.SANDBOX)
                .build();
    }

    private PaymentIntent.PaymentStatus toPaymentStatus(String status) {
        if ("COMPLETED".equals(status)) return PaymentIntent.PaymentStatus.SUCCEEDED;
        if ("CANCELED".equals(status)) return PaymentIntent.PaymentStatus.CANCELED;
        if ("APPROVED".equals(status)) return PaymentIntent.PaymentStatus.REQUIRES_CAPTURE;
        if ("PENDING".equals(status)) return PaymentIntent.PaymentStatus.PROCESSING;
        return PaymentIntent.PaymentStatus.FAILED;
    }
}
