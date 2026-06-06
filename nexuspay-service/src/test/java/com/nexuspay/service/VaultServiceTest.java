package com.nexuspay.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.common.util.CryptoUtil;
import com.nexuspay.domain.entity.VaultAuditLog;
import com.nexuspay.domain.entity.VaultEntry;
import com.nexuspay.repository.VaultAuditLogRepository;
import com.nexuspay.repository.VaultEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VaultServiceTest {

    @Mock
    private VaultEntryRepository vaultEntryRepository;

    @Mock
    private VaultAuditLogRepository vaultAuditLogRepository;

    private VaultService vaultService;
    private VaultEncryptionService encryptionService;
    private final List<VaultEntry> entries = new ArrayList<>();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        encryptionService = new VaultEncryptionService(new CryptoUtil(), new ObjectMapper());
        ReflectionTestUtils.setField(encryptionService, "masterKeyMaterial", "master-test-key");
        ReflectionTestUtils.setField(encryptionService, "custodianKeyMaterial", "custodian-test-key");
        ReflectionTestUtils.setField(encryptionService, "keyId", "test-v1");

        vaultService = new VaultService(vaultEntryRepository, vaultAuditLogRepository, encryptionService);

        when(vaultEntryRepository.save(any(VaultEntry.class))).thenAnswer(invocation -> {
            VaultEntry entry = invocation.getArgument(0);
            if (entry.getId() == null) {
                entry.setId(UUID.randomUUID());
            }
            entries.removeIf(existing -> existing.getId().equals(entry.getId()));
            entries.add(entry);
            return entry;
        });
        when(vaultAuditLogRepository.save(any(VaultAuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(vaultEntryRepository.findByTokenHash(anyString())).thenAnswer(invocation -> {
            String tokenHash = invocation.getArgument(0);
            return entries.stream().filter(entry -> tokenHash.equals(entry.getTokenHash())).findFirst();
        });
        when(vaultEntryRepository.findFirstByMerchantIdAndFingerprintAndEntryTypeAndStatus(
                any(), anyString(), any(), any())).thenAnswer(invocation -> {
            UUID merchantId = invocation.getArgument(0);
            String fingerprint = invocation.getArgument(1);
            VaultEntry.EntryType type = invocation.getArgument(2);
            VaultEntry.EntryStatus status = invocation.getArgument(3);
            return entries.stream()
                    .filter(entry -> merchantId.equals(entry.getMerchantId()))
                    .filter(entry -> fingerprint.equals(entry.getFingerprint()))
                    .filter(entry -> type == entry.getEntryType())
                    .filter(entry -> status == entry.getStatus())
                    .findFirst();
        });
    }

    @Test
    void shouldStoreCardWithoutPersistingCvc() {
        UUID merchantId = UUID.randomUUID();

        VaultService.TokenizedPaymentMethod tokenized = vaultService.storeCard(
                merchantId,
                null,
                new VaultEncryptionService.CardData(
                        "4242424242424242", "12", "2099", "123",
                        "visa", null, "Jane Doe"),
                null);

        assertTrue(tokenized.token().startsWith("vault_"));
        assertEquals("4242", tokenized.last4());

        VaultEncryptionService.DetokenizedResult detokenized = vaultService.retrieve(merchantId, tokenized.token(), null);
        assertEquals(VaultEntry.EntryType.CARD, detokenized.type());
        assertEquals("4242424242424242", detokenized.data().get("number"));
        assertFalse(detokenized.data().containsKey("cvc"));

        verify(vaultAuditLogRepository, atLeastOnce()).save(any(VaultAuditLog.class));
    }

    @Test
    void shouldReuseActiveTokenForSameMerchantAndCard() {
        UUID merchantId = UUID.randomUUID();
        var card = new VaultEncryptionService.CardData(
                "4242424242424242", "12", "2099", "123",
                "visa", null, "Jane Doe");

        VaultService.TokenizedPaymentMethod first = vaultService.storeCard(merchantId, null, card, null);
        VaultService.TokenizedPaymentMethod second = vaultService.storeCard(merchantId, null, card, null);

        assertEquals(first.token(), second.token());
        assertEquals(1, entries.size());
    }

    @Test
    void shouldRejectDetokenizeAfterRevoke() {
        UUID merchantId = UUID.randomUUID();
        VaultService.TokenizedPaymentMethod tokenized = vaultService.storeCard(
                merchantId,
                null,
                new VaultEncryptionService.CardData(
                        "4242424242424242", "12", "2099", "123",
                        "visa", null, null),
                null);

        vaultService.revoke(merchantId, tokenized.token(), null);

        assertThrows(BusinessException.class, () -> vaultService.retrieve(merchantId, tokenized.token(), null));
    }

    @Test
    void shouldNotDeduplicateAcrossMerchants() {
        var card = new VaultEncryptionService.CardData(
                "4242424242424242", "12", "2099", "123",
                "visa", null, null);

        VaultService.TokenizedPaymentMethod first = vaultService.storeCard(UUID.randomUUID(), null, card, null);
        VaultService.TokenizedPaymentMethod second = vaultService.storeCard(UUID.randomUUID(), null, card, null);

        assertNotEquals(first.token(), second.token());
        assertEquals(2, entries.size());
    }
}
