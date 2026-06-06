package com.nexuspay.repository;

import com.nexuspay.domain.entity.VaultAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VaultAuditLogRepository extends JpaRepository<VaultAuditLog, UUID> {

    List<VaultAuditLog> findByMerchantIdOrderByCreatedAtDesc(UUID merchantId);

    List<VaultAuditLog> findByVaultEntryIdOrderByCreatedAtDesc(UUID vaultEntryId);
}
