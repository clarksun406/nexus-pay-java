package com.nexuspay.domain.repository;

import com.nexuspay.domain.aggregate.connector.ConnectorAggregate;
import com.nexuspay.domain.valueobject.ProviderType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConnectorRepository {
    ConnectorAggregate save(ConnectorAggregate aggregate);
    Optional<ConnectorAggregate> findById(UUID id);
    Optional<ConnectorAggregate> findByMerchantIdAndId(UUID merchantId, UUID id);
    List<ConnectorAggregate> findByMerchantId(UUID merchantId);
    Optional<ConnectorAggregate> findPrimary(UUID merchantId);
    List<ConnectorAggregate> findByMerchantIdAndProvider(UUID merchantId, ProviderType provider);
}
