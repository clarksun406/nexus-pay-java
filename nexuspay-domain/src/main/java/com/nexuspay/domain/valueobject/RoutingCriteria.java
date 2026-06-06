package com.nexuspay.domain.valueobject;

import java.math.BigInteger;
import java.util.Set;
import java.util.UUID;

public record RoutingCriteria(
    UUID merchantId,
    Money amount,
    String countryCode,
    String paymentMethodType,
    String mode,
    Set<String> allowedProviders
) {
    public boolean matchesRule(RoutingRuleMatcher rule) {
        return rule.matches(this);
    }
}
