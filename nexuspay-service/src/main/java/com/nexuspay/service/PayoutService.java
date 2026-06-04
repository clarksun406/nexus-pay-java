package com.nexuspay.service;

import com.nexuspay.common.util.CryptoUtil;
import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.domain.entity.Payout;
import com.nexuspay.repository.PaymentIntentRepository;
import com.nexuspay.repository.PayoutRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayoutService {
    
    private final PayoutRepository payoutRepository;
    private final PaymentIntentRepository paymentIntentRepository;
    private final CryptoUtil cryptoUtil;
    
    public List<Payout> listPayouts(UUID merchantId) {
        return payoutRepository.findByMerchantId(merchantId);
    }
    
    public Payout getPayout(UUID payoutId) {
        return payoutRepository.findById(payoutId)
                .orElseThrow(() -> new IllegalArgumentException("Payout not found"));
    }
    
    @Transactional
    public void generatePayoutSummaries() {
        log.info("Generating payout summaries");
        Instant periodEnd = Instant.now().truncatedTo(ChronoUnit.HOURS);
        Instant periodStart = periodEnd.minus(1, ChronoUnit.HOURS);

        Map<PayoutGroup, List<PaymentIntent>> groups = paymentIntentRepository
                .findByStatus(PaymentIntent.PaymentStatus.SUCCEEDED)
                .stream()
                .filter(intent -> intent.getConnectorAccountId() != null)
                .filter(intent -> intent.getCreatedAt() != null
                        && !intent.getCreatedAt().isBefore(periodStart)
                        && intent.getCreatedAt().isBefore(periodEnd))
                .collect(java.util.stream.Collectors.groupingBy(
                        intent -> new PayoutGroup(
                                intent.getMerchantId(),
                                intent.getConnectorAccountId(),
                                intent.getCurrency(),
                                intent.getMode()
                        ),
                        LinkedHashMap::new,
                        java.util.stream.Collectors.toList()
                ));

        groups.forEach((group, intents) -> createPayout(
                group.merchantId(),
                group.connectorId(),
                group.currency(),
                group.mode(),
                periodStart,
                periodEnd
        ));
    }
    
    @Transactional
    public Payout createPayout(UUID merchantId, UUID connectorId, String currency, 
                               PaymentIntent.Mode mode, Instant periodStart, Instant periodEnd) {
        String idempotencyKey = generateIdempotencyKey(merchantId, connectorId, currency, mode, periodStart, periodEnd);
        
        if (payoutRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
            return payoutRepository.findByIdempotencyKey(idempotencyKey).get();
        }
        
        List<PaymentIntent> intents = paymentIntentRepository.findByMerchantId(merchantId).stream()
                .filter(i -> Objects.equals(i.getConnectorAccountId(), connectorId))
                .filter(i -> i.getMode() == mode)
                .filter(i -> i.getCurrency().equalsIgnoreCase(currency))
                .filter(i -> i.getCreatedAt() != null
                        && !i.getCreatedAt().isBefore(periodStart)
                        && i.getCreatedAt().isBefore(periodEnd))
                .toList();
        
        BigInteger totalAmount = intents.stream()
                .filter(i -> i.getStatus() == PaymentIntent.PaymentStatus.SUCCEEDED)
                .map(PaymentIntent::getAmount)
                .reduce(BigInteger.ZERO, BigInteger::add);
        
        BigInteger feeAmount = totalAmount.multiply(BigInteger.valueOf(29)).divide(BigInteger.valueOf(1000)); // 2.9%
        BigInteger netAmount = totalAmount.subtract(feeAmount);
        
        Payout payout = new Payout();
        payout.setMerchantId(merchantId);
        payout.setConnectorId(connectorId);
        payout.setCurrency(currency);
        payout.setMode(mode);
        payout.setAmount(totalAmount);
        payout.setFeeAmount(feeAmount);
        payout.setNetAmount(netAmount);
        payout.setPeriodStart(periodStart);
        payout.setPeriodEnd(periodEnd);
        payout.setIdempotencyKey(idempotencyKey);
        payout.setItemsCount((int) intents.stream()
                .filter(i -> i.getStatus() == PaymentIntent.PaymentStatus.SUCCEEDED)
                .count());
        
        return payoutRepository.save(payout);
    }
    
    private String generateIdempotencyKey(UUID merchantId, UUID connectorId, 
                                          String currency, PaymentIntent.Mode mode,
                                          Instant periodStart, Instant periodEnd) {
        return cryptoUtil.hashSha256(
            merchantId + "_" + connectorId + "_" + currency + "_" + mode + "_" + 
            periodStart.toString() + "_" + periodEnd.toString(), "payout");
    }

    private record PayoutGroup(UUID merchantId, UUID connectorId, String currency, PaymentIntent.Mode mode) {}
}
