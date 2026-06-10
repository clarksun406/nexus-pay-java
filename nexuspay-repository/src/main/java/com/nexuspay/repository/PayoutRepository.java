package com.nexuspay.repository;

import com.nexuspay.domain.entity.Payout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayoutRepository extends JpaRepository<Payout, UUID> {
    List<Payout> findByMerchantId(UUID merchantId);
    Optional<Payout> findByMerchantIdAndId(UUID merchantId, UUID id);
    Optional<Payout> findByIdempotencyKey(String idempotencyKey);
}
