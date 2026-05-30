package com.nexuspay.domain.aggregate.routing;

import com.nexuspay.domain.valueobject.ProviderType;
import com.nexuspay.domain.valueobject.RoutingRuleMatcher;
import org.junit.jupiter.api.Test;
import java.math.BigInteger;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class RoutingRuleAggregateTest {
    
    @Test
    void shouldCreateRule() {
        var matcher = new RoutingRuleMatcher(
            Set.of("USD"), null, null, null, null, 1);
        
        var rule = new RoutingRuleAggregate(
            UUID.randomUUID(), UUID.randomUUID(), 1, matcher,
            ProviderType.STRIPE, UUID.randomUUID(), null, null);
        
        assertEquals(1, rule.getPriority());
        assertTrue(rule.isEnabled());
    }
    
    @Test
    void shouldDisable() {
        var matcher = new RoutingRuleMatcher(null, null, null, null, null, 1);
        var rule = new RoutingRuleAggregate(
            UUID.randomUUID(), UUID.randomUUID(), 1, matcher,
            ProviderType.STRIPE, UUID.randomUUID(), null, null);
        
        rule.disable();
        assertFalse(rule.isEnabled());
    }
    
    @Test
    void shouldUpdatePriority() {
        var matcher = new RoutingRuleMatcher(null, null, null, null, null, 1);
        var rule = new RoutingRuleAggregate(
            UUID.randomUUID(), UUID.randomUUID(), 1, matcher,
            ProviderType.STRIPE, UUID.randomUUID(), null, null);
        
        rule.updatePriority(5);
        assertEquals(5, rule.getPriority());
    }
}
