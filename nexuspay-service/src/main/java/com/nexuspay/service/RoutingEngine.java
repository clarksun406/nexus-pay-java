package com.nexuspay.service;

import com.nexuspay.domain.aggregate.connector.ConnectorAggregate;
import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.domain.entity.ProviderAccount;
import com.nexuspay.domain.service.RoutingDomainService;
import com.nexuspay.domain.valueobject.Money;
import com.nexuspay.domain.valueobject.RoutingCriteria;
import com.nexuspay.repository.ProviderAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.UUID;

/**
 * Thin application-layer facade over {@link RoutingDomainService}.
 * Delegates all routing logic to the domain layer; only handles
 * credential lookups needed by downstream {@link ProviderDispatcher}.
 */
@Service
@RequiredArgsConstructor
public class RoutingEngine {

    private final RoutingDomainService routingDomainService;
    private final ProviderAccountRepository providerAccountRepository;

    public RoutingResult resolve(UUID merchantId, BigInteger amount, String currency,
                                 String countryCode, String paymentMethodType, PaymentIntent.Mode mode) {
        RoutingCriteria criteria = new RoutingCriteria(
                merchantId,
                Money.of(amount, currency),
                countryCode,
                paymentMethodType,
                mode != null ? mode.name() : null,
                null);

        RoutingDomainService.RoutingResult domainResult = routingDomainService.resolve(criteria);
        if (domainResult == null) return null;

        ProviderAccount primary = domainResult.primary() != null
                ? lookupAccount(merchantId, domainResult.primary().getId()) : null;
        ProviderAccount fallback = domainResult.fallback() != null
                ? lookupAccount(merchantId, domainResult.fallback().getId()) : null;

        return new RoutingResult(primary, fallback);
    }

    public RoutingResult resolveAnyAccount(UUID merchantId, PaymentIntent.Mode mode) {
        RoutingCriteria criteria = new RoutingCriteria(
                merchantId, null, null, null,
                mode != null ? mode.name() : null, null);

        RoutingDomainService.RoutingResult domainResult = routingDomainService.resolve(criteria);
        if (domainResult == null) return null;

        ProviderAccount primary = domainResult.primary() != null
                ? lookupAccount(merchantId, domainResult.primary().getId()) : null;

        return new RoutingResult(primary, null);
    }

    private ProviderAccount lookupAccount(UUID merchantId, UUID accountId) {
        return providerAccountRepository.findByMerchantIdAndId(merchantId, accountId).orElse(null);
    }

    public record RoutingResult(ProviderAccount primary, ProviderAccount fallback) {}
}
