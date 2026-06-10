package com.nexuspay.repository;

import com.nexuspay.domain.entity.PaymentLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentLinkRepository extends JpaRepository<PaymentLink, UUID> {
    List<PaymentLink> findByMerchantId(UUID merchantId);
    Optional<PaymentLink> findByMerchantIdAndId(UUID merchantId, UUID id);
    Optional<PaymentLink> findByToken(String token);
}
