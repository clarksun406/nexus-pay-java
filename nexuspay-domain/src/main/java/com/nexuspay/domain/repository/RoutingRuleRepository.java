package com.nexuspay.domain.repository;

import com.nexuspay.domain.aggregate.routing.RoutingRuleAggregate;

import java.util.List;
import java.util.UUID;

public interface RoutingRuleRepository {
    RoutingRuleAggregate save(RoutingRuleAggregate aggregate);
    List<RoutingRuleAggregate> findByMerchantIdOrderByPriority(UUID merchantId);
    void deleteById(UUID id);
}
