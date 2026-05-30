package com.nexuspay.domain.service;

import com.nexuspay.domain.aggregate.connector.ConnectorAggregate;
import com.nexuspay.domain.aggregate.routing.RoutingRuleAggregate;
import com.nexuspay.domain.repository.ConnectorRepository;
import com.nexuspay.domain.repository.RoutingRuleRepository;
import com.nexuspay.domain.valueobject.RoutingCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class RoutingDomainService {
    
    private final RoutingRuleRepository routingRuleRepository;
    private final ConnectorRepository connectorRepository;
    
    public RoutingResult resolve(RoutingCriteria criteria) {
        List<RoutingRuleAggregate> rules = routingRuleRepository.findByMerchantIdOrderByPriority(null);
        
        for (RoutingRuleAggregate rule : rules) {
            if (rule.matches(criteria)) {
                ConnectorAggregate primary = connectorRepository.findById(rule.getTargetAccountId())
                        .filter(ConnectorAggregate::isAvailable).orElse(null);
                
                if (primary == null) continue;
                
                ConnectorAggregate fallback = rule.getFallbackAccountId() != null
                        ? connectorRepository.findById(rule.getFallbackAccountId())
                                .filter(ConnectorAggregate::isAvailable).orElse(null)
                        : null;
                
                return new RoutingResult(primary, fallback);
            }
        }
        
        return resolveAnyConnector(criteria);
    }
    
    private RoutingResult resolveAnyConnector(RoutingCriteria criteria) {
        List<ConnectorAggregate> connectors = connectorRepository.findByMerchantId(null);
        List<ConnectorAggregate> available = connectors.stream()
                .filter(ConnectorAggregate::isAvailable).toList();
        
        if (available.isEmpty()) return null;
        
        ConnectorAggregate primary = available.stream()
                .filter(ConnectorAggregate::isPrimary).findFirst()
                .orElse(weightedSelect(available));
        
        return new RoutingResult(primary, null);
    }
    
    private ConnectorAggregate weightedSelect(List<ConnectorAggregate> connectors) {
        int total = connectors.stream().mapToInt(ConnectorAggregate::getWeight).sum();
        int pick = new Random().nextInt(total);
        for (ConnectorAggregate c : connectors) {
            pick -= c.getWeight();
            if (pick < 0) return c;
        }
        return connectors.get(connectors.size() - 1);
    }
    
    public record RoutingResult(ConnectorAggregate primary, ConnectorAggregate fallback) {}
}
