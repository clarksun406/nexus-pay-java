package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.domain.entity.ProviderAccount;
import com.nexuspay.repository.ProviderAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConnectorServiceTest {

    @Mock
    private ProviderAccountRepository providerAccountRepository;

    @InjectMocks
    private ConnectorService connectorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateConnectorWithDefaultValuesWhenOptionalFieldsNull() {
        UUID merchantId = UUID.randomUUID();
        ConnectorService.CreateConnectorRequest req = new ConnectorService.CreateConnectorRequest(
                ProviderAccount.Provider.STRIPE,
                null,
                "Stripe Main",
                "enc_sk",
                "enc_pk",
                null,
                null
        );

        when(providerAccountRepository.save(any(ProviderAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProviderAccount result = connectorService.create(merchantId, req);

        assertEquals(merchantId, result.getMerchantId());
        assertEquals(ProviderAccount.Provider.STRIPE, result.getProvider());
        assertEquals(ProviderAccount.Mode.TEST, result.getMode());
        assertEquals("Stripe Main", result.getLabel());
        assertEquals("enc_sk", result.getEncryptedSecretKey());
        assertEquals("enc_pk", result.getEncryptedPublishableKey());
        assertEquals(1, result.getWeight());
        assertFalse(result.getIsPrimary());

        verify(providerAccountRepository, never()).findByMerchantIdAndIsPrimaryTrue(any());
        verify(providerAccountRepository, times(1)).save(any(ProviderAccount.class));
    }

    @Test
    void shouldDemoteExistingPrimaryWhenCreatePrimaryConnector() {
        UUID merchantId = UUID.randomUUID();

        ProviderAccount existingPrimary = new ProviderAccount();
        existingPrimary.setId(UUID.randomUUID());
        existingPrimary.setMerchantId(merchantId);
        existingPrimary.setIsPrimary(true);

        ConnectorService.CreateConnectorRequest req = new ConnectorService.CreateConnectorRequest(
                ProviderAccount.Provider.SQUARE,
                ProviderAccount.Mode.LIVE,
                "Square Live",
                "enc_sk_sq",
                "enc_pk_sq",
                3,
                true
        );

        when(providerAccountRepository.findByMerchantIdAndIsPrimaryTrue(merchantId))
                .thenReturn(Optional.of(existingPrimary));
        when(providerAccountRepository.save(any(ProviderAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProviderAccount result = connectorService.create(merchantId, req);

        assertTrue(result.getIsPrimary());
        assertEquals(ProviderAccount.Mode.LIVE, result.getMode());
        assertEquals(3, result.getWeight());

        verify(providerAccountRepository).findByMerchantIdAndIsPrimaryTrue(merchantId);
        verify(providerAccountRepository, atLeast(2)).save(any(ProviderAccount.class));
        assertFalse(existingPrimary.getIsPrimary());
    }

    @Test
    void shouldUpdateConnectorFieldsAndSetPrimary() {
        UUID merchantId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID oldPrimaryId = UUID.randomUUID();

        ProviderAccount account = new ProviderAccount();
        account.setId(accountId);
        account.setMerchantId(merchantId);
        account.setLabel("Old Label");
        account.setWeight(1);
        account.setStatus(ProviderAccount.ConnectorStatus.ACTIVE);
        account.setIsPrimary(false);

        ProviderAccount oldPrimary = new ProviderAccount();
        oldPrimary.setId(oldPrimaryId);
        oldPrimary.setMerchantId(merchantId);
        oldPrimary.setIsPrimary(true);

        ConnectorService.UpdateConnectorRequest req = new ConnectorService.UpdateConnectorRequest(
                "New Label",
                5,
                true,
                ProviderAccount.ConnectorStatus.UNHEALTHY
        );

        when(providerAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(providerAccountRepository.findByMerchantIdAndIsPrimaryTrue(merchantId)).thenReturn(Optional.of(oldPrimary));
        when(providerAccountRepository.save(any(ProviderAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProviderAccount result = connectorService.update(accountId, req);

        assertEquals("New Label", result.getLabel());
        assertEquals(5, result.getWeight());
        assertEquals(ProviderAccount.ConnectorStatus.UNHEALTHY, result.getStatus());
        assertTrue(result.getIsPrimary());

        assertFalse(oldPrimary.getIsPrimary());
        verify(providerAccountRepository).save(oldPrimary);
        verify(providerAccountRepository).save(account);
    }

    @Test
    void shouldThrowNotFoundWhenUpdateMissingConnector() {
        UUID accountId = UUID.randomUUID();
        when(providerAccountRepository.findById(accountId)).thenReturn(Optional.empty());

        ConnectorService.UpdateConnectorRequest req = new ConnectorService.UpdateConnectorRequest(
                "X", 2, false, ProviderAccount.ConnectorStatus.ACTIVE);

        BusinessException ex = assertThrows(BusinessException.class, () -> connectorService.update(accountId, req));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals("Connector not found", ex.getMessage());
    }

    @Test
    void shouldListConnectorsByMerchantId() {
        UUID merchantId = UUID.randomUUID();
        when(providerAccountRepository.findByMerchantId(merchantId)).thenReturn(List.of(new ProviderAccount(), new ProviderAccount()));

        List<ProviderAccount> result = connectorService.listConnectors(merchantId);

        assertEquals(2, result.size());
        verify(providerAccountRepository).findByMerchantId(merchantId);
    }

    @Test
    void shouldGetConnectorById() {
        UUID accountId = UUID.randomUUID();
        ProviderAccount account = new ProviderAccount();
        account.setId(accountId);

        when(providerAccountRepository.findById(accountId)).thenReturn(Optional.of(account));

        ProviderAccount result = connectorService.getConnector(accountId);

        assertEquals(accountId, result.getId());
    }

    @Test
    void shouldThrowNotFoundWhenGetConnectorMissing() {
        UUID accountId = UUID.randomUUID();
        when(providerAccountRepository.findById(accountId)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> connectorService.getConnector(accountId));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals("Connector not found", ex.getMessage());
    }

    @Test
    void shouldDeleteConnectorById() {
        UUID accountId = UUID.randomUUID();

        connectorService.delete(accountId);

        verify(providerAccountRepository).deleteById(accountId);
    }
}
