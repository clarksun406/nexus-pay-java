package com.nexuspay.repository;

import com.nexuspay.domain.entity.MerchantUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MerchantUserRepository extends JpaRepository<MerchantUser, UUID> {
    List<MerchantUser> findByUserId(UUID userId);
    List<MerchantUser> findByMerchantId(UUID merchantId);
    Optional<MerchantUser> findByUserIdAndMerchantId(UUID userId, UUID merchantId);
    boolean existsByUserIdAndMerchantId(UUID userId, UUID merchantId);
}
