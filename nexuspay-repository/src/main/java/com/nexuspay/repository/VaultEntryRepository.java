package com.nexuspay.repository;

import com.nexuspay.domain.entity.VaultEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VaultEntryRepository extends JpaRepository<VaultEntry, UUID> {

    Optional<VaultEntry> findByToken(String token);

    Optional<VaultEntry> findByTokenHash(String tokenHash);

    Optional<VaultEntry> findByFingerprint(String fingerprint);

    Optional<VaultEntry> findFirstByMerchantIdAndFingerprintAndEntryTypeAndStatus(
            UUID merchantId,
            String fingerprint,
            VaultEntry.EntryType entryType,
            VaultEntry.EntryStatus status);

    List<VaultEntry> findByMerchantId(UUID merchantId);

    List<VaultEntry> findByMerchantIdAndCustomerId(UUID merchantId, UUID customerId);

    List<VaultEntry> findByMerchantIdAndEntryTypeAndStatus(UUID merchantId, VaultEntry.EntryType entryType, VaultEntry.EntryStatus status);

    List<VaultEntry> findByCustomerId(UUID customerId);

    boolean existsByFingerprint(String fingerprint);
}
