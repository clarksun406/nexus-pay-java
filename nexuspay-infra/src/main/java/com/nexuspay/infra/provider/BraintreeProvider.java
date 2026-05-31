package com.nexuspay.infra.provider;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Environment;
import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionRequest;
import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.domain.entity.ProviderAccount;
import com.nexuspay.service.PaymentIntentService;
import com.nexuspay.service.provider.PaymentProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class BraintreeProvider implements PaymentProvider {

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

    private BraintreeGateway createGateway(ProviderAccount account) {
        // TODO(ACL): Parse and decrypt credentials from account safely
        return new BraintreeGateway(
                Environment.SANDBOX,
                "merchant_id",
                "public_key",
                "private_key"
        );
    }
}