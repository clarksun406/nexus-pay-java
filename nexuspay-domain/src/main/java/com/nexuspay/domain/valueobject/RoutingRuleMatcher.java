package com.nexuspay.domain.valueobject;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Set;

public record RoutingRuleMatcher(
    Set<String> currencies,
    BigInteger amountMin,
    BigInteger amountMax,
    Set<String> countryCodes,
    Set<String> paymentMethodTypes,
    int weight
) {
    public boolean matches(RoutingCriteria criteria) {
        if (currencies != null && !currencies.isEmpty() 
                && !currencies.contains(criteria.amount().currency().getCurrencyCode())) {
            return false;
        }
        
        BigInteger amount = criteria.amount().amount();
        if (amountMin != null && amount.compareTo(amountMin) < 0) {
            return false;
        }
        if (amountMax != null && amount.compareTo(amountMax) > 0) {
            return false;
        }
        if (countryCodes != null && !countryCodes.isEmpty() 
                && criteria.countryCode() != null
                && !countryCodes.contains(criteria.countryCode())) {
            return false;
        }
        if (paymentMethodTypes != null && !paymentMethodTypes.isEmpty() 
                && criteria.paymentMethodType() != null
                && !paymentMethodTypes.contains(criteria.paymentMethodType())) {
            return false;
        }
        return true;
    }
}
