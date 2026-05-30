package com.nexuspay.repository;

import com.nexuspay.domain.entity.GatewayLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GatewayLogRepository extends JpaRepository<GatewayLog, UUID> {
    List<GatewayLog> findByMerchantIdOrderByCreatedAtDesc(UUID merchantId);
}
