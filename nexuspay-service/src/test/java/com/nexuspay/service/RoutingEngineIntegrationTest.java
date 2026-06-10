package com.nexuspay.service;

import com.nexuspay.domain.aggregate.connector.ConnectorAggregate;
import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.domain.entity.ProviderAccount;
import com.nexuspay.domain.service.RoutingDomainService;
import com.nexuspay.domain.valueobject.ProviderType;
import com.nexuspay.repository.ProviderAccountRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoutingEngineIntegrationTest {

    @Mock private RoutingDomainService routingDomainService;
    @Mock private ProviderAccountRepository providerAccountRepository;

    @InjectMocks private RoutingEngine routingEngine;

    @BeforeEach
    void setup() { MockitoAnnotations.openMocks(this); }

    @Test
    void shouldResolveViaDomainService() {
        UUID merchantId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        var aggregate = new ConnectorAggregate(accountId, merchantId, ProviderType.STRIPE, "stripe-1", "TEST");
        var domainResult = new RoutingDomainService.RoutingResult(aggregate, null);

        when(routingDomainService.resolve(any())).thenReturn(domainResult);

        var account = new ProviderAccount();
        account.setId(accountId);
        account.setProvider(ProviderAccount.Provider.STRIPE);
        when(providerAccountRepository.findByMerchantIdAndId(merchantId, accountId)).thenReturn(Optional.of(account));

        var result = routingEngine.resolve(merchantId, java.math.BigInteger.valueOf(1000), "usd",
                null, "card", PaymentIntent.Mode.TEST);

        assertNotNull(result);
        assertNotNull(result.primary());
        assertEquals(accountId, result.primary().getId());
        verify(providerAccountRepository).findByMerchantIdAndId(merchantId, accountId);
        verify(providerAccountRepository, never()).findById(any());
    }

    @Test
    void shouldNotResolveAccountOutsideMerchantScope() {
        UUID merchantId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        var aggregate = new ConnectorAggregate(accountId, merchantId, ProviderType.STRIPE, "stripe-1", "TEST");
        when(routingDomainService.resolve(any()))
                .thenReturn(new RoutingDomainService.RoutingResult(aggregate, null));
        when(providerAccountRepository.findByMerchantIdAndId(merchantId, accountId)).thenReturn(Optional.empty());

        var result = routingEngine.resolve(merchantId, java.math.BigInteger.valueOf(1000), "usd",
                null, "card", PaymentIntent.Mode.TEST);

        assertNotNull(result);
        assertNull(result.primary());
        verify(providerAccountRepository).findByMerchantIdAndId(merchantId, accountId);
        verify(providerAccountRepository, never()).findById(any());
    }

    @Test
    void shouldReturnNullWhenNoRoute() {
        UUID merchantId = UUID.randomUUID();

        when(routingDomainService.resolve(any())).thenReturn(null);

        var result = routingEngine.resolve(merchantId, java.math.BigInteger.valueOf(1000), "usd",
                null, "card", PaymentIntent.Mode.TEST);

        assertNull(result);
    }
}
