package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.common.util.CryptoUtil;
import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.domain.entity.PaymentLink;
import com.nexuspay.repository.PaymentLinkRepository;
import com.nexuspay.repository.ProviderAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentLinkServiceTest {

    @Mock
    private PaymentLinkRepository paymentLinkRepository;

    @Mock
    private PaymentIntentService paymentIntentService;

    @Mock
    private ProviderAccountRepository providerAccountRepository;

    @Mock
    private CryptoUtil cryptoUtil;

    @InjectMocks
    private PaymentLinkService paymentLinkService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreatePaymentLinkWithDefaults() {
        UUID merchantId = UUID.randomUUID();
        var req = new PaymentLinkService.CreateRequest(
                "Test Link",
                "desc",
                BigInteger.valueOf(1000),
                null,
                null,
                "https://ok",
                null
        );

        when(cryptoUtil.generateToken()).thenReturn("abcdefghijklmnopqrstuvwxyz0123456789TOKEN");
        when(paymentLinkRepository.save(any(PaymentLink.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentLink result = paymentLinkService.create(merchantId, req);

        assertEquals(merchantId, result.getMerchantId());
        assertEquals("Test Link", result.getTitle());
        assertEquals("desc", result.getDescription());
        assertEquals(BigInteger.valueOf(1000), result.getAmount());
        assertEquals("usd", result.getCurrency());
        assertEquals(PaymentIntent.Mode.TEST, result.getMode());
        assertEquals("https://ok", result.getRedirectUrl());
        assertNotNull(result.getToken());
        assertEquals(32, result.getToken().length());
    }

    @Test
    void shouldUpdatePaymentLinkFields() {
        UUID linkId = UUID.randomUUID();
        PaymentLink link = new PaymentLink();
        link.setId(linkId);
        link.setTitle("Old");
        link.setDescription("Old desc");
        link.setAmount(BigInteger.valueOf(100));
        link.setStatus(PaymentLink.LinkStatus.ACTIVE);

        var req = new PaymentLinkService.UpdateRequest(
                "New",
                "New desc",
                BigInteger.valueOf(200),
                PaymentLink.LinkStatus.INACTIVE
        );

        when(paymentLinkRepository.findById(linkId)).thenReturn(Optional.of(link));
        when(paymentLinkRepository.save(any(PaymentLink.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentLink result = paymentLinkService.update(linkId, req);

        assertEquals("New", result.getTitle());
        assertEquals("New desc", result.getDescription());
        assertEquals(BigInteger.valueOf(200), result.getAmount());
        assertEquals(PaymentLink.LinkStatus.INACTIVE, result.getStatus());
    }

    @Test
    void shouldThrowNotFoundWhenUpdateMissingLink() {
        UUID linkId = UUID.randomUUID();
        when(paymentLinkRepository.findById(linkId)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> paymentLinkService.update(linkId, new PaymentLinkService.UpdateRequest("x", "y", BigInteger.ONE, null)));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals("Payment link not found", ex.getMessage());
    }

    @Test
    void shouldDeactivateLink() {
        UUID linkId = UUID.randomUUID();
        PaymentLink link = new PaymentLink();
        link.setId(linkId);
        link.setStatus(PaymentLink.LinkStatus.ACTIVE);

        when(paymentLinkRepository.findById(linkId)).thenReturn(Optional.of(link));
        when(paymentLinkRepository.save(any(PaymentLink.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentLink result = paymentLinkService.deactivate(linkId);

        assertEquals(PaymentLink.LinkStatus.INACTIVE, result.getStatus());
    }

    @Test
    void shouldReturnLinkByTokenWhenActiveAndNotExpired() {
        PaymentLink link = new PaymentLink();
        link.setToken("tok");
        link.setStatus(PaymentLink.LinkStatus.ACTIVE);
        link.setExpiresAt(Instant.now().plusSeconds(3600));

        when(paymentLinkRepository.findByToken("tok")).thenReturn(Optional.of(link));

        PaymentLink result = paymentLinkService.getByToken("tok");

        assertEquals("tok", result.getToken());
    }

    @Test
    void shouldRejectInactiveLinkByToken() {
        PaymentLink link = new PaymentLink();
        link.setToken("tok");
        link.setStatus(PaymentLink.LinkStatus.INACTIVE);

        when(paymentLinkRepository.findByToken("tok")).thenReturn(Optional.of(link));

        BusinessException ex = assertThrows(BusinessException.class, () -> paymentLinkService.getByToken("tok"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("Payment link is inactive", ex.getMessage());
    }

    @Test
    void shouldRejectExpiredLinkByToken() {
        PaymentLink link = new PaymentLink();
        link.setToken("tok");
        link.setStatus(PaymentLink.LinkStatus.ACTIVE);
        link.setExpiresAt(Instant.now().minusSeconds(60));

        when(paymentLinkRepository.findByToken("tok")).thenReturn(Optional.of(link));

        BusinessException ex = assertThrows(BusinessException.class, () -> paymentLinkService.getByToken("tok"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("Payment link expired", ex.getMessage());
    }

    @Test
    void shouldCreateAndConfirmPaymentFromLink() {
        UUID merchantId = UUID.randomUUID();

        PaymentLink link = new PaymentLink();
        link.setToken("tok");
        link.setMerchantId(merchantId);
        link.setAmount(BigInteger.valueOf(5000));
        link.setCurrency("usd");
        link.setMode(PaymentIntent.Mode.TEST);
        link.setTitle("Order #1");
        link.setStatus(PaymentLink.LinkStatus.ACTIVE);

        PaymentIntent created = new PaymentIntent();
        created.setId(UUID.randomUUID());

        PaymentIntent confirmed = new PaymentIntent();
        confirmed.setId(created.getId());
        confirmed.setStatus(PaymentIntent.PaymentStatus.SUCCEEDED);

        when(paymentLinkRepository.findByToken("tok")).thenReturn(Optional.of(link));
        when(paymentIntentService.create(eq(merchantId), any(PaymentIntentService.CreateRequest.class))).thenReturn(created);
        when(paymentIntentService.confirm(eq(merchantId), eq(created.getId()), any(PaymentIntentService.ConfirmRequest.class)))
                .thenReturn(confirmed);

        PaymentIntent result = paymentLinkService.createPaymentFromLink("tok", "card", "pm_123");

        assertEquals(PaymentIntent.PaymentStatus.SUCCEEDED, result.getStatus());

        ArgumentCaptor<PaymentIntentService.CreateRequest> createCaptor =
                ArgumentCaptor.forClass(PaymentIntentService.CreateRequest.class);
        verify(paymentIntentService).create(eq(merchantId), createCaptor.capture());

        PaymentIntentService.CreateRequest createReq = createCaptor.getValue();
        assertEquals(BigInteger.valueOf(5000), createReq.amount());
        assertEquals("usd", createReq.currency());
        assertEquals(PaymentIntent.Mode.TEST, createReq.mode());
        assertEquals(PaymentIntent.CaptureMethod.AUTOMATIC, createReq.captureMethod());
        assertEquals("Order #1", createReq.description());

        ArgumentCaptor<PaymentIntentService.ConfirmRequest> confirmCaptor =
                ArgumentCaptor.forClass(PaymentIntentService.ConfirmRequest.class);
        verify(paymentIntentService).confirm(eq(merchantId), eq(created.getId()), confirmCaptor.capture());
        assertEquals("card", confirmCaptor.getValue().paymentMethodType());
        assertEquals("pm_123", confirmCaptor.getValue().paymentMethodId());
    }

    @Test
    void shouldListAndGetPaymentLinks() {
        UUID merchantId = UUID.randomUUID();
        UUID linkId = UUID.randomUUID();

        PaymentLink one = new PaymentLink();
        one.setId(linkId);

        when(paymentLinkRepository.findByMerchantId(merchantId)).thenReturn(List.of(one));
        when(paymentLinkRepository.findById(linkId)).thenReturn(Optional.of(one));

        assertEquals(1, paymentLinkService.list(merchantId).size());
        assertEquals(linkId, paymentLinkService.get(linkId).getId());
    }
}
