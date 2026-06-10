package com.nexuspay.repository;

import com.nexuspay.domain.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefundRepository extends JpaRepository<Refund, UUID> {
    List<Refund> findByMerchantId(UUID merchantId);
    Optional<Refund> findByMerchantIdAndId(UUID merchantId, UUID id);
    List<Refund> findByPaymentIntentId(UUID paymentIntentId);
}
