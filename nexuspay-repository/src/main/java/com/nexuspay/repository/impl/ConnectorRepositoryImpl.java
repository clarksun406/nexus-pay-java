package com.nexuspay.repository.impl;

import com.nexuspay.domain.aggregate.connector.ConnectorAggregate;
import com.nexuspay.domain.entity.ProviderAccount;
import com.nexuspay.domain.repository.ConnectorRepository;
import com.nexuspay.domain.valueobject.ProviderType;
import com.nexuspay.repository.ProviderAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ConnectorRepositoryImpl implements ConnectorRepository {

    private final ProviderAccountRepository jpaRepository;

    @Override
    public ConnectorAggregate save(ConnectorAggregate aggregate) {
        ProviderAccount entity = toEntity(aggregate);
        jpaRepository.save(entity);
        return aggregate;
    }

    @Override
    public Optional<ConnectorAggregate> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toAggregate);
    }

    @Override
    public List<ConnectorAggregate> findByMerchantId(UUID merchantId) {
        return jpaRepository.findByMerchantId(merchantId).stream().map(this::toAggregate).toList();
    }

    @Override
    public Optional<ConnectorAggregate> findPrimary(UUID merchantId) {
        return jpaRepository.findByMerchantIdAndIsPrimaryTrue(merchantId).map(this::toAggregate);
    }

    @Override
    public List<ConnectorAggregate> findByMerchantIdAndProvider(UUID merchantId, ProviderType provider) {
        return jpaRepository.findByMerchantId(merchantId).stream()
                .filter(a -> a.getProvider().name().equals(provider.name()))
                .map(this::toAggregate).toList();
    }

    private ProviderAccount toEntity(ConnectorAggregate agg) {
        ProviderAccount entity = new ProviderAccount();
        entity.setId(agg.getId());
        entity.setMerchantId(agg.getMerchantId());
        entity.setProvider(ProviderAccount.Provider.valueOf(agg.getProvider().name()));
        entity.setLabel(agg.getLabel());
        entity.setMode(agg.getMode() != null ? ProviderAccount.Mode.valueOf(agg.getMode().toUpperCase()) : ProviderAccount.Mode.TEST);
        entity.setIsPrimary(agg.isPrimary());
        entity.setWeight(agg.getWeight());
        entity.setStatus(ProviderAccount.ConnectorStatus.valueOf(agg.getStatus().name()));
        return entity;
    }

    private ConnectorAggregate toAggregate(ProviderAccount entity) {
        ConnectorAggregate agg = new ConnectorAggregate(
                entity.getId(), entity.getMerchantId(),
                ProviderType.valueOf(entity.getProvider().name()),
                entity.getLabel(),
                entity.getMode() != null ? entity.getMode().name() : "TEST");
        agg.setPrimary(entity.getIsPrimary() != null && entity.getIsPrimary());
        agg.setWeight(entity.getWeight() != null ? entity.getWeight() : 1);
        if (entity.getStatus() == ProviderAccount.ConnectorStatus.INACTIVE
                || entity.getStatus() == ProviderAccount.ConnectorStatus.UNHEALTHY) {
            agg.deactivate();
        }
        return agg;
    }
}