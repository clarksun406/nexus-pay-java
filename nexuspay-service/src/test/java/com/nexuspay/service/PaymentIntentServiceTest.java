package com.nexuspay.service;

import com.nexuspay.domain.entity.*;
import com.nexuspay.domain.aggregate.payment.PaymentIntentAggregate;
import com.nexuspay.domain.service.PaymentDomainService;
import com.nexuspay.domain.valueobject.Money;
import com.nexuspay.repository.PaymentIntentRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.math.BigInteger;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentIntentServiceTest {
    
    @Mock private PaymentIntentRepository paymentIntentRepository;
    @Mock private PaymentDomainService paymentDomainService;
    @Mock private RoutingEngine routingEngine;
    @Mock private ProviderDispatcher providerDispatcher;
    @Mock private OutboxService outboxService;
    
    @InjectMocks private PaymentIntentService service;
    
    @BeforeEach
    void setup() { MockitoAnnotations.openMocks(this); }
    
    @Test
    void shouldCreatePaymentIntent() {
        UUID merchantId = UUID.randomUUID();
        
        var intent = new PaymentIntent();
        intent.setId(UUID.randomUUID());
        when(paymentDomainService.createPaymentIntent(eq(merchantId), any(Money.class), eq("key"), eq(false)))
                .thenReturn(new PaymentIntentAggregate(
                        UUID.randomUUID(),
                        merchantId,
                        Money.of(BigInteger.valueOf(1000), "USD"),
                        "key",
                        false
                ));
        when(paymentIntentRepository.save(any())).thenReturn(intent);
        
        var result = service.create(merchantId, new PaymentIntentService.CreateRequest(
            BigInteger.valueOf(1000), "USD", PaymentIntent.Mode.TEST,
            PaymentIntent.CaptureMethod.AUTOMATIC, "key", null, null, null, null, null));
        
        assertNotNull(result);
    }
    
    @Test
    void shouldReturnExistingForIdempotencyKey() {
        UUID merchantId = UUID.randomUUID();
        var existing = new PaymentIntent();
        existing.setId(UUID.randomUUID());
        
        when(paymentIntentRepository.findByMerchantIdAndIdempotencyKey(merchantId, "key"))
            .thenReturn(Optional.of(existing));
        
        var result = service.create(merchantId, new PaymentIntentService.CreateRequest(
            BigInteger.valueOf(1000), "USD", PaymentIntent.Mode.TEST,
            PaymentIntent.CaptureMethod.AUTOMATIC, "key", null, null, null, null, null));
        
        assertEquals(existing.getId(), result.getId());
        verify(paymentIntentRepository, never()).save(any());
    }
}
