package com.nexuspay.infra.provider;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Environment;
import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionRequest;
import com.nexuspay.common.util.AesGcmEncryptionService;
import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.domain.entity.ProviderAccount;
import com.nexuspay.domain.entity.Refund;
import com.nexuspay.service.PaymentIntentService;
import com.nexuspay.service.provider.PaymentProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class BraintreeProvider implements PaymentProvider {

    private final AesGcmEncryptionService encryptionService;

    @Override
    public ProviderAccount.Provider supportedProvider() {
        return ProviderAccount.Provider.BRAINTREE;
    }

    @Override
    public PaymentIntentService.ChargeResult charge(PaymentIntent intent, String paymentMethodId, ProviderAccount account) {
        try {
            BraintreeGateway gateway = createGateway(account);

            TransactionRequest request = new TransactionRequest()
                    .amount(new BigDecimal(intent.getAmount()).divide(new BigDecimal("100")))
                    .paymentMethodNonce(paymentMethodId)
                    .orderId(intent.getIdempotencyKey())
                    .options()
                    .submitForSettlement(intent.getCaptureMethod() != PaymentIntent.CaptureMethod.MANUAL)
                    .done();

            Result<Transaction> result = gateway.transaction().sale(request);

            if (result.isSuccess()) {
                Transaction transaction = result.getTarget();
                return new PaymentIntentService.ChargeResult(
                        true,
                        transaction.getId(),
                        "{\"status\": \"" + transaction.getStatus() + "\"}",
                        null, null
                );
            } else {
                String error = result.getMessage();
                log.error("Braintree charge failed: {}", error);
                return new PaymentIntentService.ChargeResult(
                        false, null, null, "BRAINTREE_ERROR", error
                );
            }

        } catch (Exception e) {
            log.error("Braintree charge failed: {}", e.getMessage(), e);
            return new PaymentIntentService.ChargeResult(false, null, null, "BRAINTREE_ERROR", e.getMessage());
        }
    }

    @Override
    public boolean capture(String providerPaymentId, ProviderAccount account) {
        try {
            BraintreeGateway gateway = createGateway(account);
            Result<Transaction> result = gateway.transaction().submitForSettlement(providerPaymentId);
            return result.isSuccess();
        } catch (Exception e) {
            log.error("Braintree capture failed: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean cancel(String providerPaymentId, ProviderAccount account) {
        try {
            BraintreeGateway gateway = createGateway(account);
            Result<Transaction> result = gateway.transaction().voidTransaction(providerPaymentId);
            return result.isSuccess();
        } catch (Exception e) {
            log.error("Braintree cancel failed: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public RefundResult refund(String providerPaymentId, BigInteger amount, String currency,
                               Refund.RefundReason reason, ProviderAccount account) {
        try {
            BraintreeGateway gateway = createGateway(account);
            Result<Transaction> result = gateway.transaction().refund(
                    providerPaymentId,
                    new BigDecimal(amount).divide(new BigDecimal("100"))
            );

            if (result.isSuccess()) {
                Transaction refund = result.getTarget();
                return new RefundResult(
                        true,
                        refund.getId(),
                        "{\"status\": \"" + refund.getStatus() + "\"}",
                        null,
                        null
                );
            }

            return new RefundResult(false, null, null, "BRAINTREE_REFUND_ERROR", result.getMessage());
        } catch (Exception e) {
            log.error("Braintree refund failed: {}", e.getMessage(), e);
            return new RefundResult(false, null, null, "BRAINTREE_REFUND_ERROR", e.getMessage());
        }
    }

    @Override
    public ProviderPaymentStatus fetchPaymentStatus(String providerPaymentId, ProviderAccount account) {
        try {
            BraintreeGateway gateway = createGateway(account);
            Transaction transaction = gateway.transaction().find(providerPaymentId);
            return new ProviderPaymentStatus(
                    transaction.getId(),
                    toPaymentStatus(transaction.getStatus()),
                    transaction.getAmount() != null
                            ? transaction.getAmount().movePointRight(2).toBigInteger()
                            : null,
                    transaction.getCurrencyIsoCode(),
                    "{\"status\": \"" + transaction.getStatus() + "\"}"
            );
        } catch (Exception e) {
            log.error("Braintree status fetch failed: {}", e.getMessage(), e);
            return new ProviderPaymentStatus(providerPaymentId, PaymentIntent.PaymentStatus.FAILED, null, null,
                    "{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private BraintreeGateway createGateway(ProviderAccount account) {
        return new BraintreeGateway(
                account.getMode() == ProviderAccount.Mode.LIVE ? Environment.PRODUCTION : Environment.SANDBOX,
                account.getConnectorAccountId(),
                encryptionService.decrypt(account.getEncryptedPublishableKey()),
                encryptionService.decrypt(account.getEncryptedSecretKey())
        );
    }

    private PaymentIntent.PaymentStatus toPaymentStatus(Transaction.Status status) {
        if (status == Transaction.Status.SETTLED || status == Transaction.Status.SETTLING
                || status == Transaction.Status.SUBMITTED_FOR_SETTLEMENT) {
            return PaymentIntent.PaymentStatus.SUCCEEDED;
        }
        if (status == Transaction.Status.AUTHORIZED) {
            return PaymentIntent.PaymentStatus.REQUIRES_CAPTURE;
        }
        if (status == Transaction.Status.VOIDED) {
            return PaymentIntent.PaymentStatus.CANCELED;
        }
        if (status == Transaction.Status.PROCESSOR_DECLINED || status == Transaction.Status.GATEWAY_REJECTED
                || status == Transaction.Status.FAILED) {
            return PaymentIntent.PaymentStatus.FAILED;
        }
        return PaymentIntent.PaymentStatus.PROCESSING;
    }
}
