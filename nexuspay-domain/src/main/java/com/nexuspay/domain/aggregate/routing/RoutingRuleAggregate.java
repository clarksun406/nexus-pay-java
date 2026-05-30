package com.nexuspay.domain.aggregate.routing;

import com.nexuspay.domain.valueobject.ProviderType;
import com.nexuspay.domain.valueobject.RoutingCriteria;
import com.nexuspay.domain.valueobject.RoutingRuleMatcher;
import lombok.Getter;

import java.util.UUID;

@Getter
public class RoutingRuleAggregate {
    
    private final UUID id;
    private final UUID merchantId;
    private int priority;
    private boolean enabled;
    private final RoutingRuleMatcher matcher;
    private final ProviderType targetProvider;
    private final UUID targetAccountId;
    private final ProviderType fallbackProvider;
    private final UUID fallbackAccountId;
    
    public RoutingRuleAggregate(UUID id, UUID merchantId, int priority, RoutingRuleMatcher matcher,
                                ProviderType targetProvider, UUID targetAccountId,
                                ProviderType fallbackProvider, UUID fallbackAccountId) {
        this.id = id;
        this.merchantId = merchantId;
        this.priority = priority;
        this.enabled = true;
        this.matcher = matcher;
        this.targetProvider = targetProvider;
        this.targetAccountId = targetAccountId;
        this.fallbackProvider = fallbackProvider;
        this.fallbackAccountId = fallbackAccountId;
    }
    
    public boolean matches(RoutingCriteria criteria) {
        return enabled && matcher.matches(criteria);
    }
    
    public void enable() { this.enabled = true; }
    public void disable() { this.enabled = false; }
    public void updatePriority(int p) { this.priority = p; }
}
