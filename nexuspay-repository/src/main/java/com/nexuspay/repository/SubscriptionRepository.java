package com.nexuspay.repository;

import com.nexuspay.domain.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    
    List<Subscription> findByCustomerId(UUID customerId);
    
    List<Subscription> findByMerchantId(UUID merchantId);
    
    Optional<Subscription> findByMerchantIdAndId(UUID merchantId, UUID id);
    
    List<Subscription> findByStatus(Subscription.SubscriptionStatus status);
    
    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.currentPeriodEnd <= :before")
    List<Subscription> findDueForRenewal(Instant before);
    
    Optional<Subscription> findByProviderSubscriptionId(String providerSubscriptionId);
}
