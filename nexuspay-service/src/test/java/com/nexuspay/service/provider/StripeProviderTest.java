package com.nexuspay.service.provider;

import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.domain.entity.ProviderAccount;
import com.nexuspay.service.PaymentIntentService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntentCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class StripeProviderTest {
    
    private final StripeProvider stripeProvider = new StripeProvider();
    
    @Test
    void shouldHandleInvalidApiKey() {
        PaymentIntent intent = new PaymentIntent();
        intent.setId(java.util.UUID.randomUUID());
        intent.setAmount(java.math.BigInteger.valueOf(1000));
        intent.setCurrency("USD");
        intent.setCaptureMethod(PaymentIntent.CaptureMethod.AUTOMATIC);
        
        ProviderAccount account = new ProviderAccount();
        account.setEncryptedSecretKey("sk_test_invalid");
        
        var result = stripeProvider.charge(intent, "pm_card_visa", account);
        
        // With invalid key, should fail gracefully
        assertFalse(result.success());
        assertNotNull(result.failureCode());
    }
}
