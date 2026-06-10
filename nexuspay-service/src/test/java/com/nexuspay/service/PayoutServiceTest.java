package com.nexuspay.service;

import com.nexuspay.common.util.CryptoUtil;
import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.domain.entity.Payout;
import com.nexuspay.repository.PaymentIntentRepository;
import com.nexuspay.repository.PayoutRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigInteger;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PayoutServiceTest {

    @Mock private PayoutRepository payoutRepo;
    @Mock private PaymentIntentRepository paymentIntentRepo;
    @Mock private ProviderDispatcher providerDispatcher;
    @Mock private CryptoUtil cryptoUtil;
    @InjectMocks private PayoutService payoutService;

    @BeforeEach
    void setUp() { MockitoAnnotations.openMocks(this); }

    @Test
    void shouldListPayoutsByMerchant() {
        UUID merchantId = UUID.randomUUID();
        Payout payout = new Payout();
        payout.setMerchantId(merchantId);
        payout.setAmount(BigInteger.valueOf(5000));

        when(payoutRepo.findByMerchantId(merchantId)).thenReturn(List.of(payout));

        List<Payout> payouts = payoutService.listPayouts(merchantId);
        assertEquals(1, payouts.size());
        assertEquals(BigInteger.valueOf(5000), payouts.get(0).getAmount());
    }

    @Test
    void shouldGetPayoutById() {
        UUID payoutId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        Payout payout = new Payout();
        payout.setId(payoutId);
        payout.setMerchantId(merchantId);
        payout.setAmount(BigInteger.valueOf(3000));

        when(payoutRepo.findByMerchantIdAndId(merchantId, payoutId)).thenReturn(Optional.of(payout));

        Payout result = payoutService.getPayout(merchantId, payoutId);
        assertNotNull(result);
        assertEquals(payoutId, result.getId());
    }

    @Test
    void shouldGeneratePayoutSummaries() {
        when(paymentIntentRepo.findByMerchantId(any())).thenReturn(List.of());
        when(payoutRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        payoutService.generatePayoutSummaries();
        // Should not throw - empty data is valid
    }

    @Test
    void shouldCreatePayout() {
        UUID merchantId = UUID.randomUUID();
        UUID connectorId = UUID.randomUUID();
        Instant now = java.time.Instant.now();

        when(paymentIntentRepo.findByMerchantId(merchantId)).thenReturn(List.of());
        when(payoutRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Payout payout = payoutService.createPayout(merchantId, connectorId, "USD",
                PaymentIntent.Mode.TEST, now, now.plusSeconds(3600));
        assertNotNull(payout);
        assertEquals(merchantId, payout.getMerchantId());
    }
}
