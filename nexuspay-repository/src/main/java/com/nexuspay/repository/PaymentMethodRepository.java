package com.nexuspay.repository;

import com.nexuspay.domain.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID> {
    
    List<PaymentMethod> findByCustomerId(UUID customerId);
    
    List<PaymentMethod> findByMerchantId(UUID merchantId);
    
    Optional<PaymentMethod> findByCustomerIdAndId(UUID customerId, UUID id);
    
    Optional<PaymentMethod> findByProviderPaymentMethodId(String providerPaymentMethodId);
    
    List<PaymentMethod> findByCustomerIdAndStatus(UUID customerId, PaymentMethod.PaymentMethodStatus status);
    
    Optional<PaymentMethod> findByCustomerIdAndIsDefaultTrue(UUID customerId);
}
