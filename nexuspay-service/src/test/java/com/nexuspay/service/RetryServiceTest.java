package com.nexuspay.service;

import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.domain.entity.PaymentRequest;
import com.nexuspay.domain.entity.ProviderAccount;
import com.nexuspay.repository.PaymentIntentRepository;
import com.nexuspay.repository.PaymentRequestRepository;
import com.nexuspay.repository.ProviderAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RetryServiceTest {

    @Mock
    private PaymentIntentRepository paymentIntentRepository;
    @Mock
    private PaymentRequestRepository paymentRequestRepository;
    @Mock
    private ProviderAccountRepository providerAccountRepository;
    @Mock
    private PaymentIntentService paymentIntentService;
    @Mock
    private DeclineCodeService declineCodeService;
    @Mock
    private RoutingEngine routingEngine;
    @Mock
    private ProviderDispatcher providerDispatcher;

    @InjectMocks
    private RetryService retryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldThrowWhenPaymentIntentNotFound() {
        UUID id = UUID.randomUUID();
        when(paymentIntentRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> retryService.executeRetry(id));
    }

    @Test
    void shouldReturnIntentWhenStatusIsNotFailed() {
        UUID id = UUID.randomUUID();
        PaymentIntent intent = baseIntent(id);
        intent.setStatus(PaymentIntent.PaymentStatus.SUCCEEDED);

        when(paymentIntentRepository.findById(id)).thenReturn(Optional.of(intent));

        PaymentIntent result = retryService.executeRetry(id);

        assertEquals(PaymentIntent.PaymentStatus.SUCCEEDED, result.getStatus());
        verifyNoInteractions(paymentRequestRepository, declineCodeService, providerDispatcher);
    }

    @Test
    void shouldReturnIntentWhenNoRequests() {
        UUID id = UUID.randomUUID();
        PaymentIntent intent = baseIntent(id);

        when(paymentIntentRepository.findById(id)).thenReturn(Optional.of(intent));
        when(paymentRequestRepository.findByPaymentIntentIdOrderByCreatedAtDesc(id)).thenReturn(List.of());

        PaymentIntent result = retryService.executeRetry(id);

        assertSame(intent, result);
    }

    @Test
    void shouldReturnIntentWhenLastFailureCodeNull() {
        UUID id = UUID.randomUUID();
        PaymentIntent intent = baseIntent(id);
        PaymentRequest last = new PaymentRequest();
        last.setFailureCode(null);

        when(paymentIntentRepository.findById(id)).thenReturn(Optional.of(intent));
        when(paymentRequestRepository.findByPaymentIntentIdOrderByCreatedAtDesc(id)).thenReturn(List.of(last));

        PaymentIntent result = retryService.executeRetry(id);

        assertSame(intent, result);
        verifyNoInteractions(declineCodeService);
    }

    @Test
    void shouldReturnIntentWhenStrategySaysNoRetry() {
        UUID id = UUID.randomUUID();
        PaymentIntent intent = baseIntent(id);
        PaymentRequest last = failedRequest(id, "card_declined", "card");

        when(paymentIntentRepository.findById(id)).thenReturn(Optional.of(intent));
        when(paymentRequestRepository.findByPaymentIntentIdOrderByCreatedAtDesc(id)).thenReturn(List.of(last));
        when(declineCodeService.getRetryStrategy("card_declined", 1))
                .thenReturn(new DeclineCodeService.RetryStrategy(false, 0L, false));

        PaymentIntent result = retryService.executeRetry(id);

        assertSame(intent, result);
        verifyNoInteractions(providerAccountRepository, providerDispatcher);
    }

    @Test
    void shouldReturnIntentWhenRetryWithoutFallback() {
        UUID id = UUID.randomUUID();
        PaymentIntent intent = baseIntent(id);
        PaymentRequest last = failedRequest(id, "processing_error", "card");

        when(paymentIntentRepository.findById(id)).thenReturn(Optional.of(intent));
        when(paymentRequestRepository.findByPaymentIntentIdOrderByCreatedAtDesc(id)).thenReturn(List.of(last));
        when(declineCodeService.getRetryStrategy("processing_error", 1))
                .thenReturn(new DeclineCodeService.RetryStrategy(true, 1000L, false));

        PaymentIntent result = retryService.executeRetry(id);

        assertSame(intent, result);
        verifyNoInteractions(providerAccountRepository, providerDispatcher);
    }

    @Test
    void shouldReturnIntentWhenNoFallbackProviderAvailable() {
        UUID id = UUID.randomUUID();
        PaymentIntent intent = baseIntent(id);
        PaymentRequest last = failedRequest(id, "processing_error", "card");

        ProviderAccount sameAsFailed = new ProviderAccount();
        sameAsFailed.setId(intent.getConnectorAccountId());
        sameAsFailed.setProvider(ProviderAccount.Provider.STRIPE);

        when(paymentIntentRepository.findById(id)).thenReturn(Optional.of(intent));
        when(paymentRequestRepository.findByPaymentIntentIdOrderByCreatedAtDesc(id)).thenReturn(List.of(last));
        when(declineCodeService.getRetryStrategy("processing_error", 1))
                .thenReturn(new DeclineCodeService.RetryStrategy(true, 1000L, true));
        when(providerAccountRepository.findByMerchantIdAndModeAndStatus(
                intent.getMerchantId(), intent.getMode(), ProviderAccount.ConnectorStatus.ACTIVE))
                .thenReturn(List.of(sameAsFailed));

        PaymentIntent result = retryService.executeRetry(id);

        assertSame(intent, result);
        verify(providerDispatcher, never()).charge(any(), any(), anyString());
    }

    @Test
    void shouldFallbackAndSucceedWithAutomaticCapture() {
        UUID id = UUID.randomUUID();
        PaymentIntent intent = baseIntent(id);
        intent.setCaptureMethod(PaymentIntent.CaptureMethod.AUTOMATIC);

        PaymentRequest last = failedRequest(id, "processing_error", "card");

        UUID fallbackId = UUID.randomUUID();
        ProviderAccount failed = new ProviderAccount();
        failed.setId(intent.getConnectorAccountId());
        failed.setProvider(ProviderAccount.Provider.STRIPE);

        ProviderAccount fallback = new ProviderAccount();
        fallback.setId(fallbackId);
        fallback.setProvider(ProviderAccount.Provider.SQUARE);

        when(paymentIntentRepository.findById(id)).thenReturn(Optional.of(intent));
        when(paymentRequestRepository.findByPaymentIntentIdOrderByCreatedAtDesc(id)).thenReturn(List.of(last));
        when(declineCodeService.getRetryStrategy("processing_error", 1))
                .thenReturn(new DeclineCodeService.RetryStrategy(true, 1000L, true));
        when(providerAccountRepository.findByMerchantIdAndModeAndStatus(
                intent.getMerchantId(), intent.getMode(), ProviderAccount.ConnectorStatus.ACTIVE))
                .thenReturn(List.of(failed, fallback));
        when(providerDispatcher.charge(eq(ProviderAccount.Provider.SQUARE), eq(intent), eq("card")))
                .thenReturn(new PaymentIntentService.ChargeResult(true, "pay_1", "{\"ok\":1}", null, null));
        when(paymentIntentRepository.save(intent)).thenReturn(intent);

        PaymentIntent result = retryService.executeRetry(id);

        assertEquals(PaymentIntent.PaymentStatus.SUCCEEDED, result.getStatus());
        assertEquals(fallbackId, result.getConnectorAccountId());
        assertEquals(PaymentIntent.Provider.SQUARE, result.getResolvedProvider());
        assertEquals("pay_1", result.getProviderPaymentId());
        verify(paymentIntentRepository).save(intent);
    }

    @Test
    void shouldFallbackAndCreateFailedRequestWhenChargeFails() {
        UUID id = UUID.randomUUID();
        PaymentIntent intent = baseIntent(id);
        PaymentRequest last = failedRequest(id, "processing_error", "card");

        ProviderAccount failed = new ProviderAccount();
        failed.setId(intent.getConnectorAccountId());
        failed.setProvider(ProviderAccount.Provider.STRIPE);

        ProviderAccount fallback = new ProviderAccount();
        fallback.setId(UUID.randomUUID());
        fallback.setProvider(ProviderAccount.Provider.BRAINTREE);

        when(paymentIntentRepository.findById(id)).thenReturn(Optional.of(intent));
        when(paymentRequestRepository.findByPaymentIntentIdOrderByCreatedAtDesc(id)).thenReturn(List.of(last));
        when(declineCodeService.getRetryStrategy("processing_error", 1))
                .thenReturn(new DeclineCodeService.RetryStrategy(true, 1000L, true));
        when(providerAccountRepository.findByMerchantIdAndModeAndStatus(
                intent.getMerchantId(), intent.getMode(), ProviderAccount.ConnectorStatus.ACTIVE))
                .thenReturn(List.of(failed, fallback));
        when(providerDispatcher.charge(eq(ProviderAccount.Provider.BRAINTREE), eq(intent), eq("card")))
                .thenReturn(new PaymentIntentService.ChargeResult(false, null, null, "ERR", "failed"));
        when(paymentIntentRepository.save(intent)).thenReturn(intent);

        PaymentIntent result = retryService.executeRetry(id);

        assertEquals(PaymentIntent.PaymentStatus.PROCESSING, result.getStatus());
        verify(paymentRequestRepository).save(any(PaymentRequest.class));
        verify(paymentIntentRepository).save(intent);
    }

    private PaymentIntent baseIntent(UUID id) {
        PaymentIntent intent = new PaymentIntent();
        intent.setId(id);
        intent.setMerchantId(UUID.randomUUID());
        intent.setMode(PaymentIntent.Mode.TEST);
        intent.setStatus(PaymentIntent.PaymentStatus.FAILED);
        intent.setCaptureMethod(PaymentIntent.CaptureMethod.AUTOMATIC);
        intent.setConnectorAccountId(UUID.randomUUID());
        intent.setAmount(BigInteger.valueOf(1000));
        intent.setCurrency("usd");
        return intent;
    }

    private PaymentRequest failedRequest(UUID paymentIntentId, String failureCode, String paymentMethodType) {
        PaymentRequest req = new PaymentRequest();
        req.setPaymentIntentId(paymentIntentId);
        req.setFailureCode(failureCode);
        req.setPaymentMethodType(paymentMethodType);
        req.setStatus(PaymentRequest.RequestStatus.FAILED);
        return req;
    }
}
