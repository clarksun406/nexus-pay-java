package com.nexuspay.repository;

import com.nexuspay.domain.entity.Dispute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, UUID> {
    List<Dispute> findByMerchantId(UUID merchantId);
    Optional<Dispute> findByProviderDisputeId(String providerDisputeId);
}
