package com.nexuspay.domain.aggregate.connector;

import com.nexuspay.domain.valueobject.ProviderType;
import lombok.Getter;

import java.util.UUID;

@Getter
public class ConnectorAggregate {
    
    private final UUID id;
    private final UUID merchantId;
    private final ProviderType provider;
    private final String label;
    private int weight;
    private boolean primary;
    private ConnectorStatus status;
    
    public ConnectorAggregate(UUID id, UUID merchantId, ProviderType provider, String label) {
        this.id = id;
        this.merchantId = merchantId;
        this.provider = provider;
        this.label = label;
        this.weight = 1;
        this.status = ConnectorStatus.ACTIVE;
    }
    
    public void setPrimary(boolean primary) { this.primary = primary; }
    public void setWeight(int weight) { this.weight = Math.max(1, weight); }
    public void activate() { this.status = ConnectorStatus.ACTIVE; }
    public void deactivate() { this.status = ConnectorStatus.INACTIVE; }
    
    public boolean isAvailable() {
        return status == ConnectorStatus.ACTIVE;
    }
    
    public enum ConnectorStatus { ACTIVE, INACTIVE, UNHEALTHY }
}
