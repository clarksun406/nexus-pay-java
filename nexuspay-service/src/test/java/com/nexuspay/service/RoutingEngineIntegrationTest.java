package com.nexuspay.service;

import com.nexuspay.domain.entity.ProviderAccount;
import com.nexuspay.repository.ProviderAccountRepository;
import com.nexuspay.repository.RoutingRuleRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.math.BigInteger;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoutingEngineTest {
    
    @Mock private RoutingRuleRepository routingRuleRepository;
    @Mock private ProviderAccountRepository providerAccountRepository;
    
    @InjectMocks private RoutingEngine routingEngine;
    
    @BeforeEach
    void setup() { MockitoAnnotations.openMocks(this); }
    
    @Test
    void shouldResolveAnyAccount() {
        UUID merchantId = UUID.randomUUID();
        
        var account = new ProviderAccount();
        account.setId(UUID.randomUUID());
        account.setProvider(ProviderAccount.Provider.STRIPE);
        account.setIsPrimary(true);
        account.setStatus(ProviderAccount.ConnectorStatus.ACTIVE);
        
        when(providerAccountRepository.findByMerchantIdAndModeAndStatus(
            merchantId, PaymentIntent.Mode.TEST, ProviderAccount.ConnectorStatus.ACTIVE))
            .thenReturn(List.of(account));
        
        var result = routingEngine.resolveAnyAccount(merchantId, PaymentIntent.Mode.TEST);
        
        assertNotNull(result);
        assertEquals(account.getId(), result.getId());
    }
    
    @Test
    void shouldReturnNullWhenNoAccounts() {
        UUID merchantId = UUID.randomUUID();
        
        when(providerAccountRepository.findByMerchantIdAndModeAndStatus(
            merchantId, PaymentIntent.Mode.TEST, ProviderAccount.ConnectorStatus.ACTIVE))
            .thenReturn(Collections.emptyList());
        
        var result = routingEngine.resolveAnyAccount(merchantId, PaymentIntent.Mode.TEST);
        
        assertNull(result);
    }
}
