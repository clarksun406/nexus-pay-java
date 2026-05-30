package com.nexuspay.repository;

import com.nexuspay.domain.entity.RoutingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoutingRuleRepository extends JpaRepository<RoutingRule, UUID> {
    List<RoutingRule> findByMerchantIdOrderByPriorityAsc(UUID merchantId);
    List<RoutingRule> findByMerchantIdAndEnabledTrueOrderByPriorityAsc(UUID merchantId);
}
