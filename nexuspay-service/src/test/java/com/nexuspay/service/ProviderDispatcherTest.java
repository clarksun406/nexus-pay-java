package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.domain.entity.ProviderAccount;
import com.nexuspay.repository.ProviderAccountRepository;
import com.nexuspay.service.provider.PaymentProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProviderDispatcherTest {

    @Mock
    private ProviderAccountRepository providerAccountRepository;

    @Mock
    private PaymentProvider stripeProvider;

    @Mock
    private PaymentProvider squareProvider;

    @Mock
    private PaymentProvider braintreeProvider;

    private ProviderDispatcher providerDispatcher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(stripeProvider.supportedProvider()).thenReturn(ProviderAccount.Provider.STRIPE);
        when(squareProvider.supportedProvider()).thenReturn(ProviderAccount.Provider.SQUARE);
        when(braintreeProvider.supportedProvider()).thenReturn(ProviderAccount.Provider.BRAINTREE);

        providerDispatcher = new ProviderDispatcher(
                providerAccountRepository,
                List.of(stripeProvider, squareProvider, braintreeProvider)
        );
        providerDispatcher.init();

        clearInvocations(stripeProvider, squareProvider, braintreeProvider);
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
                providerAccountRepository,
                List.of(stripeProvider, squareProvider, braintreeProvider)
        );

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
                providerAccountRepository,
                List.of(stripeProvider, squareProvider, braintreeProvider)
        );

        boolean result = dispatcherWithoutInit.capture(ProviderAccount.Provider.STRIPE, "pay_1", accountId);

        assertFalse(result);
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

    // ---- refund contract tests ----

    @Test
    void shouldDelegateRefundToStripeProvider() {
        UUID accountId = UUID.randomUUID();
        ProviderAccount account = new ProviderAccount();
        account.setId(accountId);

        BigInteger amount = BigInteger.valueOf(500);
        PaymentProvider.RefundResult expected =
                new PaymentProvider.RefundResult(true, "re_123", "{\"status\":\"succeeded\"}", null, null);

        when(providerAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(stripeProvider.refund("pi_1", amount, "USD", null, account)).thenReturn(expected);

        PaymentProvider.RefundResult result = providerDispatcher.refund(
                ProviderAccount.Provider.STRIPE, "pi_1", amount, "USD", null, accountId);

        assertEquals(expected, result);
        verify(stripeProvider).refund("pi_1", amount, "USD", null, account);
    }

    @Test
    void shouldDelegateRefundToSquareProvider() {
        UUID accountId = UUID.randomUUID();
        ProviderAccount account = new ProviderAccount();
        account.setId(accountId);

        BigInteger amount = BigInteger.valueOf(1000);
        PaymentProvider.RefundResult expected =
                new PaymentProvider.RefundResult(true, "sq_re_1", "{\"status\":\"COMPLETED\"}", null, null);

        when(providerAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(squareProvider.refund("sq_pay_1", amount, "USD", null, account)).thenReturn(expected);

        PaymentProvider.RefundResult result = providerDispatcher.refund(
                ProviderAccount.Provider.SQUARE, "sq_pay_1", amount, "USD", null, accountId);

        assertEquals(expected, result);
        verify(squareProvider).refund("sq_pay_1", amount, "USD", null, account);
    }

    @Test
    void shouldThrowBadRequestWhenRefundProviderMissing() {
        UUID accountId = UUID.randomUUID();
        when(providerAccountRepository.findById(accountId)).thenReturn(Optional.of(new ProviderAccount()));

        ProviderDispatcher dispatcherWithoutInit = new ProviderDispatcher(
                providerAccountRepository,
                List.of()
        );

        BusinessException ex = assertThrows(BusinessException.class,
                () -> dispatcherWithoutInit.refund(ProviderAccount.Provider.STRIPE, "pi_1",
                        BigInteger.ONE, "USD", null, accountId));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("Unsupported provider"));
    }

    @Test
    void shouldThrowBadRequestWhenRefundNotSupported() {
        UUID accountId = UUID.randomUUID();
        ProviderAccount account = new ProviderAccount();
        account.setId(accountId);

        when(providerAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(stripeProvider.refund(any(), any(), any(), any(), any()))
                .thenThrow(new UnsupportedOperationException("Refund is not supported for STRIPE"));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> providerDispatcher.refund(ProviderAccount.Provider.STRIPE, "pi_1",
                        BigInteger.ONE, "USD", null, accountId));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    // ---- fetchPaymentStatus contract tests ----

    @Test
    void shouldDelegateFetchPaymentStatusToSquareProvider() {
        UUID accountId = UUID.randomUUID();
        ProviderAccount account = new ProviderAccount();
        account.setId(accountId);

        PaymentProvider.ProviderPaymentStatus expected = new PaymentProvider.ProviderPaymentStatus(
                "sq_pay_1", PaymentIntent.PaymentStatus.SUCCEEDED,
                BigInteger.valueOf(2000), "USD", "{\"status\":\"COMPLETED\"}");

        when(providerAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(squareProvider.fetchPaymentStatus("sq_pay_1", account)).thenReturn(expected);

        PaymentProvider.ProviderPaymentStatus result =
                providerDispatcher.fetchPaymentStatus(ProviderAccount.Provider.SQUARE, "sq_pay_1", accountId);

        assertEquals(expected, result);
        verify(squareProvider).fetchPaymentStatus("sq_pay_1", account);
    }

    @Test
    void shouldDelegateFetchPaymentStatusToBraintreeProvider() {
        UUID accountId = UUID.randomUUID();
        ProviderAccount account = new ProviderAccount();
        account.setId(accountId);

        PaymentProvider.ProviderPaymentStatus expected = new PaymentProvider.ProviderPaymentStatus(
                "bt_txn_1", PaymentIntent.PaymentStatus.SUCCEEDED,
                BigInteger.valueOf(3000), "USD", "{\"status\":\"SETTLED\"}");

        when(providerAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(braintreeProvider.fetchPaymentStatus("bt_txn_1", account)).thenReturn(expected);

        PaymentProvider.ProviderPaymentStatus result =
                providerDispatcher.fetchPaymentStatus(ProviderAccount.Provider.BRAINTREE, "bt_txn_1", accountId);

        assertEquals(expected, result);
        verify(braintreeProvider).fetchPaymentStatus("bt_txn_1", account);
    }

    @Test
    void shouldThrowBadRequestWhenStatusFetchNotSupported() {
        UUID accountId = UUID.randomUUID();
        ProviderAccount account = new ProviderAccount();
        account.setId(accountId);

        when(providerAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(stripeProvider.fetchPaymentStatus(any(), any()))
                .thenThrow(new UnsupportedOperationException("Status fetch is not supported for STRIPE"));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> providerDispatcher.fetchPaymentStatus(ProviderAccount.Provider.STRIPE, "pi_1", accountId));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }
}