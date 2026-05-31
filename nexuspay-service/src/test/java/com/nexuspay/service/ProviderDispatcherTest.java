package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.domain.entity.ProviderAccount;
import com.nexuspay.repository.ProviderAccountRepository;
import com.nexuspay.service.provider.BraintreeProvider;
import com.nexuspay.service.provider.SquareProvider;
import com.nexuspay.service.provider.StripeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProviderDispatcherTest {

    @Mock
    private ProviderAccountRepository providerAccountRepository;

    @Mock
    private StripeProvider stripeProvider;

    @Mock
    private SquareProvider squareProvider;

    @Mock
    private BraintreeProvider braintreeProvider;

    @InjectMocks
    private ProviderDispatcher providerDispatcher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        providerDispatcher.init();
    }

    @Test
    void shouldDelegateChargeToStripeProvider() {
        UUID accountId = UUID.randomUUID();
        PaymentIntent intent = new PaymentIntent();
        intent.setConnectorAccountId(accountId);

        ProviderAccount account = new ProviderAccount();
        account.setId(accountId);

        PaymentIntentService.ChargeResult expected =
                new PaymentIntentService.ChargeResult(true, "pi_123", "{\"ok\":true}", null, null);

        when(providerAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(stripeProvider.charge(intent, "pm_123", account)).thenReturn(expected);

        PaymentIntentService.ChargeResult result =
                providerDispatcher.charge(ProviderAccount.Provider.STRIPE, intent, "pm_123");

        assertEquals(expected, result);
        verify(stripeProvider).charge(intent, "pm_123", account);
        verifyNoInteractions(squareProvider, braintreeProvider);
    }

    @Test
    void shouldThrowNotFoundWhenChargeAccountMissing() {
        UUID accountId = UUID.randomUUID();
        PaymentIntent intent = new PaymentIntent();
        intent.setConnectorAccountId(accountId);

        when(providerAccountRepository.findById(accountId)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> providerDispatcher.charge(ProviderAccount.Provider.STRIPE, intent, "pm_123"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals("Provider account not found", ex.getMessage());
    }

    @Test
    void shouldThrowBadRequestWhenChargeProviderMapNotInitialized() {
        UUID accountId = UUID.randomUUID();
        PaymentIntent intent = new PaymentIntent();
        intent.setConnectorAccountId(accountId);

        ProviderAccount account = new ProviderAccount();
        account.setId(accountId);

        when(providerAccountRepository.findById(accountId)).thenReturn(Optional.of(account));

        ProviderDispatcher dispatcherWithoutInit = new ProviderDispatcher(
                providerAccountRepository, stripeProvider, squareProvider, braintreeProvider);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> dispatcherWithoutInit.charge(ProviderAccount.Provider.STRIPE, intent, "pm_123"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("Unsupported provider"));
    }

    @Test
    void shouldDelegateCaptureToSquareProvider() {
        UUID accountId = UUID.randomUUID();
        ProviderAccount account = new ProviderAccount();
        account.setId(accountId);

        when(providerAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(squareProvider.capture("pay_1", account)).thenReturn(true);

        boolean result = providerDispatcher.capture(ProviderAccount.Provider.SQUARE, "pay_1", accountId);

        assertTrue(result);
        verify(squareProvider).capture("pay_1", account);
    }

    @Test
    void shouldReturnFalseWhenCaptureProviderMapNotInitialized() {
        UUID accountId = UUID.randomUUID();
        ProviderAccount account = new ProviderAccount();
        account.setId(accountId);

        when(providerAccountRepository.findById(accountId)).thenReturn(Optional.of(account));

        ProviderDispatcher dispatcherWithoutInit = new ProviderDispatcher(
                providerAccountRepository, stripeProvider, squareProvider, braintreeProvider);

        boolean result = dispatcherWithoutInit.capture(ProviderAccount.Provider.STRIPE, "pay_1", accountId);

        assertFalse(result);
        verifyNoInteractions(stripeProvider, squareProvider, braintreeProvider);
    }

    @Test
    void shouldDelegateCancelToBraintreeProvider() {
        UUID accountId = UUID.randomUUID();
        ProviderAccount account = new ProviderAccount();
        account.setId(accountId);

        when(providerAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(braintreeProvider.cancel("pay_1", account)).thenReturn(true);

        boolean result = providerDispatcher.cancel(ProviderAccount.Provider.BRAINTREE, "pay_1", accountId);

        assertTrue(result);
        verify(braintreeProvider).cancel("pay_1", account);
    }

    @Test
    void shouldThrowNotFoundWhenCancelAccountMissing() {
        UUID accountId = UUID.randomUUID();
        when(providerAccountRepository.findById(accountId)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> providerDispatcher.cancel(ProviderAccount.Provider.BRAINTREE, "pay_1", accountId));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals("Provider account not found", ex.getMessage());
    }
}

