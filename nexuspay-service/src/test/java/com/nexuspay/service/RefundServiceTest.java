package com.nexuspay.service;

import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.domain.entity.ProviderAccount;
import com.nexuspay.repository.PaymentIntentRepository;
import com.nexuspay.repository.RefundRepository;
import com.nexuspay.service.provider.PaymentProvider;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.math.BigInteger;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RefundServiceTest {
    
    @Mock private RefundRepository refundRepository;
    @Mock private PaymentIntentRepository paymentIntentRepository;
    @Mock private ProviderDispatcher providerDispatcher;
    
    @InjectMocks private RefundService refundService;
    
    @Test
    void shouldCreateRefund() {
        UUID merchantId = UUID.randomUUID();
        UUID intentId = UUID.randomUUID();
        
        var intent = new PaymentIntent();
        intent.setId(intentId);
        intent.setMerchantId(merchantId);
        intent.setStatus(PaymentIntent.PaymentStatus.SUCCEEDED);
        intent.setAmount(BigInteger.valueOf(1000));
        intent.setCurrency("USD");
        intent.setResolvedProvider(PaymentIntent.Provider.STRIPE);
        intent.setConnectorAccountId(UUID.randomUUID());
        intent.setProviderPaymentId("pi_123");
        
        when(paymentIntentRepository.findById(intentId)).thenReturn(Optional.of(intent));
        when(providerDispatcher.refund(
                eq(ProviderAccount.Provider.STRIPE),
                eq("pi_123"),
                eq(BigInteger.valueOf(1000)),
                eq("USD"),
                isNull(),
                eq(intent.getConnectorAccountId())
        )).thenReturn(new PaymentProvider.RefundResult(true, "re_123", "{\"status\":\"succeeded\"}", null, null));
        
        var refund = new com.nexuspay.domain.entity.Refund();
        refund.setId(UUID.randomUUID());
        when(refundRepository.save(any())).thenReturn(refund);
        
        var result = refundService.create(merchantId, 
            new RefundService.CreateRefundRequest(intentId, null, null));
        
        assertNotNull(result);
    }
}
