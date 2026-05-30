package com.nexuspay.service;

import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.domain.entity.ProviderAccount;
import com.nexuspay.domain.entity.RoutingRule;
import com.nexuspay.repository.ProviderAccountRepository;
import com.nexuspay.repository.RoutingRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RoutingEngine {
    
    private final RoutingRuleRepository routingRuleRepository;
    private final ProviderAccountRepository providerAccountRepository;
    
    public RoutingResult resolve(UUID merchantId, BigInteger amount, String currency, 
                                 String countryCode, String paymentMethodType, PaymentIntent.Mode mode) {
        
        List<RoutingRule> rules = routingRuleRepository.findByMerchantIdAndEnabledTrueOrderByPriorityAsc(merchantId);
        
        for (RoutingRule rule : rules) {
            if (matches(rule, amount, currency, countryCode, paymentMethodType)) {
                ProviderAccount primary = resolveAccount(rule.getTargetAccountId(), mode);
                if (primary == null) continue;
                
                ProviderAccount fallback = rule.getFallbackAccountId() != null 
                        ? resolveAccount(rule.getFallbackAccountId(), mode) 
                        : null;
                
                return new RoutingResult(primary, fallback);
            }
        }
        
        return resolveAnyAccount(merchantId, mode);
    }
    
    public ProviderAccount resolveAnyAccount(UUID merchantId, PaymentIntent.Mode mode) {
        List<ProviderAccount> accounts = providerAccountRepository
                .findByMerchantIdAndModeAndStatus(merchantId, mode, ProviderAccount.ConnectorStatus.ACTIVE);
        
        if (accounts.isEmpty()) return null;
        
        Optional<ProviderAccount> primary = accounts.stream().filter(a -> a.getIsPrimary()).findFirst();
        return primary.orElseGet(() -> weightedSelect(accounts));
    }
    
    private boolean matches(RoutingRule rule, BigInteger amount, String currency, 
                           String countryCode, String paymentMethodType) {
        if (rule.getCurrencies() != null && !Arrays.asList(rule.getCurrencies().split(",")).contains(currency)) {
            return false;
        }
        if (rule.getAmountMin() != null && amount.compareTo(rule.getAmountMin()) < 0) {
            return false;
        }
        if (rule.getAmountMax() != null && amount.compareTo(rule.getAmountMax()) > 0) {
            return false;
        }
        if (rule.getCountryCodes() != null && countryCode != null 
                && !Arrays.asList(rule.getCountryCodes().split(",")).contains(countryCode)) {
            return false;
        }
        if (rule.getPaymentMethodTypes() != null && paymentMethodType != null 
                && !Arrays.asList(rule.getPaymentMethodTypes().split(",")).contains(paymentMethodType)) {
            return false;
        }
        return true;
    }
    
    private ProviderAccount resolveAccount(UUID accountId, PaymentIntent.Mode mode) {
        return providerAccountRepository.findById(accountId)
                .filter(a -> a.getStatus() == ProviderAccount.ConnectorStatus.ACTIVE)
                .filter(a -> a.getMode() == mode || mode == null)
                .orElse(null);
    }
    
    private ProviderAccount weightedSelect(List<ProviderAccount> accounts) {
        int totalWeight = accounts.stream().mapToInt(ProviderAccount::getWeight).sum();
        int pick = new Random().nextInt(totalWeight);
        for (ProviderAccount account : accounts) {
            pick -= account.getWeight();
            if (pick < 0) return account;
        }
        return accounts.get(accounts.size() - 1);
    }
    
    public record RoutingResult(ProviderAccount primary, ProviderAccount fallback) {}
}
