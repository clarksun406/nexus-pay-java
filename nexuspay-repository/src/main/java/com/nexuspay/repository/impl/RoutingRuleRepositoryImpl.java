package com.nexuspay.repository.impl;

import com.nexuspay.domain.aggregate.routing.RoutingRuleAggregate;
import com.nexuspay.domain.entity.RoutingRule;
import com.nexuspay.domain.repository.RoutingRuleRepository;
import com.nexuspay.domain.valueobject.ProviderType;
import com.nexuspay.domain.valueobject.RoutingRuleMatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoutingRuleRepositoryImpl implements RoutingRuleRepository {

    private final com.nexuspay.repository.RoutingRuleRepository jpaRepository;

    @Override
    public RoutingRuleAggregate save(RoutingRuleAggregate aggregate) {
        RoutingRule entity = toEntity(aggregate);
        jpaRepository.save(entity);
        return aggregate;
    }

    @Override
    public List<RoutingRuleAggregate> findByMerchantIdOrderByPriority(UUID merchantId) {
        return jpaRepository.findByMerchantIdAndEnabledTrueOrderByPriorityAsc(merchantId)
                .stream().map(this::toAggregate).toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    private RoutingRule toEntity(RoutingRuleAggregate agg) {
        RoutingRule entity = new RoutingRule();
        entity.setId(agg.getId());
        entity.setMerchantId(agg.getMerchantId());
        entity.setPriority(agg.getPriority());
        entity.setEnabled(agg.isEnabled());
        RoutingRuleMatcher matcher = agg.getMatcher();
        if (matcher.currencies() != null && !matcher.currencies().isEmpty()) {
            entity.setCurrencies(String.join(",", matcher.currencies()));
        }
        entity.setAmountMin(matcher.amountMin());
        entity.setAmountMax(matcher.amountMax());
        if (matcher.countryCodes() != null && !matcher.countryCodes().isEmpty()) {
            entity.setCountryCodes(String.join(",", matcher.countryCodes()));
        }
        if (matcher.paymentMethodTypes() != null && !matcher.paymentMethodTypes().isEmpty()) {
            entity.setPaymentMethodTypes(String.join(",", matcher.paymentMethodTypes()));
        }
        entity.setWeight(matcher.weight());
        entity.setTargetProvider(com.nexuspay.domain.entity.PaymentIntent.Provider.valueOf(agg.getTargetProvider().name()));
        entity.setTargetAccountId(agg.getTargetAccountId());
        if (agg.getFallbackProvider() != null) {
            entity.setFallbackProvider(com.nexuspay.domain.entity.PaymentIntent.Provider.valueOf(agg.getFallbackProvider().name()));
        }
        entity.setFallbackAccountId(agg.getFallbackAccountId());
        return entity;
    }

    private RoutingRuleAggregate toAggregate(RoutingRule entity) {
        Set<String> currencies = splitToSet(entity.getCurrencies());
        Set<String> countryCodes = splitToSet(entity.getCountryCodes());
        Set<String> paymentMethodTypes = splitToSet(entity.getPaymentMethodTypes());

        RoutingRuleMatcher matcher = new RoutingRuleMatcher(
                currencies, entity.getAmountMin(), entity.getAmountMax(),
                countryCodes, paymentMethodTypes,
                entity.getWeight() != null ? entity.getWeight() : 1);

        ProviderType targetProvider = ProviderType.valueOf(entity.getTargetProvider().name());
        ProviderType fallbackProvider = entity.getFallbackProvider() != null
                ? ProviderType.valueOf(entity.getFallbackProvider().name()) : null;

        RoutingRuleAggregate agg = new RoutingRuleAggregate(
                entity.getId(), entity.getMerchantId(), entity.getPriority(),
                matcher, targetProvider, entity.getTargetAccountId(),
                fallbackProvider, entity.getFallbackAccountId());

        if (entity.getEnabled() != null && !entity.getEnabled()) {
            agg.disable();
        }
        return agg;
    }

    private Set<String> splitToSet(String csv) {
        if (csv == null || csv.isBlank()) return Set.of();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }
}