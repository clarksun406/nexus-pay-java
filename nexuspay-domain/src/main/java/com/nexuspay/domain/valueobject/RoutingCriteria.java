package com.nexuspay.domain.valueobject;

import java.math.BigInteger;
import java.util.Set;

public record RoutingCriteria(
    Money amount,
    String countryCode,
    String paymentMethodType,
    Set<String> allowedProviders
) {
    public boolean matchesRule(RoutingRuleMatcher rule) {
        return rule.matches(this);
    }
}
