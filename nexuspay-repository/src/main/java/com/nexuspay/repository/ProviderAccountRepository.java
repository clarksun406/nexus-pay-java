package com.nexuspay.repository;

import com.nexuspay.domain.entity.ProviderAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProviderAccountRepository extends JpaRepository<ProviderAccount, UUID> {
    List<ProviderAccount> findByMerchantId(UUID merchantId);
    List<ProviderAccount> findByMerchantIdAndStatus(UUID merchantId, ProviderAccount.ConnectorStatus status);
    Optional<ProviderAccount> findByMerchantIdAndIsPrimaryTrue(UUID merchantId);
    List<ProviderAccount> findByMerchantIdAndModeAndStatus(UUID merchantId, ProviderAccount.Mode mode, ProviderAccount.ConnectorStatus status);
}
