package com.nexuspay.service.provider;

import com.braintreegateway.*;
import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.domain.entity.ProviderAccount;
import com.nexuspay.service.PaymentIntentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
public class BraintreeProvider implements PaymentProvider {
    
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
        // Parse credentials from account.getEncryptedCredentials() JSON
        return new BraintreeGateway(
                Environment.SANDBOX,
                "merchant_id",
                "public_key",
                "private_key"
        );
    }
    
    private String decryptKey(String encrypted) {
        return encrypted;
    }
}
