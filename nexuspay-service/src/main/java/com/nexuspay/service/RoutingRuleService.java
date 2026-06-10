package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.domain.entity.RoutingRule;
import com.nexuspay.repository.RoutingRuleRepository;
import com.nexuspay.repository.ProviderAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoutingRuleService {
    
    private final RoutingRuleRepository routingRuleRepository;
    private final ProviderAccountRepository providerAccountRepository;
    
    @Transactional
    public RoutingRule create(UUID merchantId, CreateRuleRequest req) {
        validateConnectorOwnership(merchantId, req.targetAccountId());
        validateConnectorOwnership(merchantId, req.fallbackAccountId());

        RoutingRule rule = new RoutingRule();
        rule.setMerchantId(merchantId);
        rule.setPriority(req.priority());
        rule.setEnabled(req.enabled() != null ? req.enabled() : true);
        rule.setCurrencies(req.currencies());
        rule.setAmountMin(req.amountMin());
        rule.setAmountMax(req.amountMax());
        rule.setCountryCodes(req.countryCodes());
        rule.setPaymentMethodTypes(req.paymentMethodTypes());
        rule.setTargetProvider(req.targetProvider());
        rule.setTargetAccountId(req.targetAccountId());
        rule.setFallbackProvider(req.fallbackProvider());
        rule.setFallbackAccountId(req.fallbackAccountId());
        rule.setWeight(req.weight() != null ? req.weight() : 1);
        
        return routingRuleRepository.save(rule);
    }
    
    @Transactional
    public RoutingRule update(UUID merchantId, UUID ruleId, UpdateRuleRequest req) {
        RoutingRule rule = routingRuleRepository.findByMerchantIdAndId(merchantId, ruleId)
                .orElseThrow(() -> new BusinessException("Rule not found", HttpStatus.NOT_FOUND));
        
        if (req.priority() != null) rule.setPriority(req.priority());
        if (req.enabled() != null) rule.setEnabled(req.enabled());
        if (req.currencies() != null) rule.setCurrencies(req.currencies());
        if (req.amountMin() != null) rule.setAmountMin(req.amountMin());
        if (req.amountMax() != null) rule.setAmountMax(req.amountMax());
        if (req.countryCodes() != null) rule.setCountryCodes(req.countryCodes());
        if (req.paymentMethodTypes() != null) rule.setPaymentMethodTypes(req.paymentMethodTypes());
        if (req.weight() != null) rule.setWeight(req.weight());
        
        return routingRuleRepository.save(rule);
    }
    
    public List<RoutingRule> listRules(UUID merchantId) {
        return routingRuleRepository.findByMerchantIdOrderByPriorityAsc(merchantId);
    }
    
    public RoutingRule getRule(UUID merchantId, UUID ruleId) {
        return routingRuleRepository.findByMerchantIdAndId(merchantId, ruleId)
                .orElseThrow(() -> new BusinessException("Rule not found", HttpStatus.NOT_FOUND));
    }
    
    @Transactional
    public void delete(UUID merchantId, UUID ruleId) {
        RoutingRule rule = getRule(merchantId, ruleId);
        routingRuleRepository.delete(rule);
    }

    private void validateConnectorOwnership(UUID merchantId, UUID accountId) {
        if (accountId == null) {
            return;
        }
        providerAccountRepository.findByMerchantIdAndId(merchantId, accountId)
                .orElseThrow(() -> new BusinessException("Connector not found", HttpStatus.NOT_FOUND));
    }
    
    public record CreateRuleRequest(Integer priority, Boolean enabled, String currencies,
                                   java.math.BigInteger amountMin, java.math.BigInteger amountMax,
                                   String countryCodes, String paymentMethodTypes,
                                   PaymentIntent.Provider targetProvider, UUID targetAccountId,
                                   PaymentIntent.Provider fallbackProvider, UUID fallbackAccountId,
                                   Integer weight) {}
    
    public record UpdateRuleRequest(Integer priority, Boolean enabled, String currencies,
                                   java.math.BigInteger amountMin, java.math.BigInteger amountMax,
                                   String countryCodes, String paymentMethodTypes, Integer weight) {}
}
