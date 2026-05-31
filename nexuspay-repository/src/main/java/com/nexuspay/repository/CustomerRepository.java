package com.nexuspay.repository;

import com.nexuspay.domain.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    
    List<Customer> findByMerchantId(UUID merchantId);
    
    Optional<Customer> findByMerchantIdAndId(UUID merchantId, UUID id);
    
    Optional<Customer> findByMerchantIdAndEmail(UUID merchantId, String email);
    
    Optional<Customer> findByProviderCustomerId(String providerCustomerId);
    
    boolean existsByMerchantIdAndEmail(UUID merchantId, String email);
}
