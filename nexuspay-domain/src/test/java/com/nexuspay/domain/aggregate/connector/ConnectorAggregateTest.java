package com.nexuspay.domain.aggregate.connector;

import com.nexuspay.domain.valueobject.ProviderType;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ConnectorAggregateTest {
    
    @Test
    void shouldCreateConnector() {
        var connector = new ConnectorAggregate(
            UUID.randomUUID(), UUID.randomUUID(), ProviderType.STRIPE, "Main Stripe");
        
        assertEquals(ProviderType.STRIPE, connector.getProvider());
        assertEquals("Main Stripe", connector.getLabel());
        assertTrue(connector.isAvailable());
    }
    
    @Test
    void shouldSetPrimary() {
        var connector = new ConnectorAggregate(
            UUID.randomUUID(), UUID.randomUUID(), ProviderType.STRIPE, "Main");
        
        connector.setPrimary(true);
        assertTrue(connector.isPrimary());
    }
    
    @Test
    void shouldDeactivate() {
        var connector = new ConnectorAggregate(
            UUID.randomUUID(), UUID.randomUUID(), ProviderType.STRIPE, "Main");
        
        connector.deactivate();
        assertFalse(connector.isAvailable());
    }
    
    @Test
    void shouldSetWeight() {
        var connector = new ConnectorAggregate(
            UUID.randomUUID(), UUID.randomUUID(), ProviderType.STRIPE, "Main");
        
        connector.setWeight(5);
        assertEquals(5, connector.getWeight());
    }
    
    @Test
    void shouldNotSetZeroWeight() {
        var connector = new ConnectorAggregate(
            UUID.randomUUID(), UUID.randomUUID(), ProviderType.STRIPE, "Main");
        
        connector.setWeight(0);
        assertEquals(1, connector.getWeight()); // minimum is 1
    }
}
