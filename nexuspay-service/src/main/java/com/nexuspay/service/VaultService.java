package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.domain.entity.VaultAuditLog;
import com.nexuspay.domain.entity.VaultEntry;
import com.nexuspay.repository.VaultAuditLogRepository;
import com.nexuspay.repository.VaultEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VaultService {

    private final VaultEntryRepository vaultEntryRepository;
    private final VaultAuditLogRepository vaultAuditLogRepository;
    private final VaultEncryptionService encryptionService;

    @Transactional
    public TokenizedPaymentMethod storeCard(UUID merchantId, UUID customerId,
                                            VaultEncryptionService.CardData card,
                                            AuditContext auditContext) {
        validateCard(card);
        var result = encryptionService.tokenizeCard(merchantId, customerId, card);
        return saveOrDedup(merchantId, customerId, result, VaultEntry.EntryType.CARD, auditContext);
    }

    @Transactional
    public TokenizedPaymentMethod storeBankAccount(UUID merchantId, UUID customerId,
                                                   VaultEncryptionService.BankAccountData bank,
                                                   AuditContext auditContext) {
        validateBankAccount(bank);
        var result = encryptionService.tokenizeBankAccount(merchantId, customerId, bank);
        return saveOrDedup(merchantId, customerId, result, VaultEntry.EntryType.BANK_ACCOUNT, auditContext);
    }

    @Transactional
    public TokenizedPaymentMethod storeWallet(UUID merchantId, UUID customerId,
                                              VaultEncryptionService.WalletData wallet,
                                              AuditContext auditContext) {
        validateWallet(wallet);
        var result = encryptionService.tokenizeWallet(merchantId, customerId, wallet);
        return saveOrDedup(merchantId, customerId, result, VaultEntry.EntryType.WALLET, auditContext);
    }

    @Transactional
    public VaultEncryptionService.DetokenizedResult retrieve(UUID merchantId, String token, AuditContext auditContext) {
        VaultEntry entry = getActiveEntry(merchantId, token);
        entry.setLastUsedAt(Instant.now());
        vaultEntryRepository.save(entry);
        audit(entry, VaultAuditLog.Action.DETOKENIZE, auditContext);
        return encryptionService.detokenize(entry);
    }

    @Transactional
    public void revoke(UUID merchantId, String token, AuditContext auditContext) {
        VaultEntry entry = getActiveEntry(merchantId, token);
        entry.setStatus(VaultEntry.EntryStatus.REVOKED);
        vaultEntryRepository.save(entry);
        audit(entry, VaultAuditLog.Action.REVOKE, auditContext);
    }

    @Transactional(readOnly = true)
    public List<VaultEntry> listByMerchant(UUID merchantId) {
        return vaultEntryRepository.findByMerchantId(merchantId);
    }

    @Transactional(readOnly = true)
    public List<VaultEntry> listByCustomer(UUID merchantId, UUID customerId) {
        return vaultEntryRepository.findByMerchantIdAndCustomerId(merchantId, customerId);
    }

    @Transactional(readOnly = true)
    public List<VaultEntry> listActiveCards(UUID merchantId) {
        return vaultEntryRepository.findByMerchantIdAndEntryTypeAndStatus(
                merchantId, VaultEntry.EntryType.CARD, VaultEntry.EntryStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<VaultAuditLog> listAuditLogs(UUID merchantId) {
        return vaultAuditLogRepository.findByMerchantIdOrderByCreatedAtDesc(merchantId);
    }

    private TokenizedPaymentMethod saveOrDedup(
            UUID merchantId,
            UUID customerId,
            VaultEncryptionService.TokenizedResult result,
            VaultEntry.EntryType type,
            AuditContext auditContext) {
        var existing = vaultEntryRepository.findFirstByMerchantIdAndFingerprintAndEntryTypeAndStatus(
                merchantId, result.fingerprint(), type, VaultEntry.EntryStatus.ACTIVE);
        if (existing.isPresent()) {
            VaultEntry entry = existing.get();
            audit(entry, VaultAuditLog.Action.READ, auditContext);
            return toTokenizedPaymentMethod(entry);
        }

        VaultEntry entry = new VaultEntry();
        entry.setMerchantId(merchantId);
        entry.setCustomerId(customerId);
        entry.setEntryType(type);
        entry.setToken(result.token());
        entry.setTokenHash(encryptionService.hashToken(result.token()));
        entry.setEncryptedData(result.encryptedData());
        entry.setEncryptedDataKey(result.encryptedDataKey());
        entry.setKeyId(result.keyId());
        entry.setEncryptionAlgorithm(result.encryptionAlgorithm());
        entry.setKeyModel(result.keyModel());
        entry.setDataSignature(result.signature());
        entry.setFingerprint(result.fingerprint());
        entry.setBrand(result.brand());
        entry.setLast4(result.last4());
        VaultEntry saved = vaultEntryRepository.save(entry);
        audit(saved, VaultAuditLog.Action.TOKENIZE, auditContext);

        return toTokenizedPaymentMethod(saved);
    }

    private VaultEntry getActiveEntry(UUID merchantId, String token) {
        if (token == null || token.isBlank()) {
            throw new BusinessException("Vault token is required", HttpStatus.BAD_REQUEST);
        }

        String tokenHash = encryptionService.hashToken(token);
        VaultEntry entry = vaultEntryRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException("Vault token not found", HttpStatus.NOT_FOUND));
        if (!entry.getMerchantId().equals(merchantId)) {
            throw new BusinessException("Vault token not found", HttpStatus.NOT_FOUND);
        }
        if (entry.getStatus() != VaultEntry.EntryStatus.ACTIVE) {
            throw new BusinessException("Vault token is " + entry.getStatus().name().toLowerCase(Locale.ROOT),
                    HttpStatus.BAD_REQUEST);
        }
        if (entry.getExpiresAt() != null && entry.getExpiresAt().isBefore(Instant.now())) {
            entry.setStatus(VaultEntry.EntryStatus.EXPIRED);
            vaultEntryRepository.save(entry);
            throw new BusinessException("Vault token is expired", HttpStatus.BAD_REQUEST);
        }
        return entry;
    }

    private void audit(VaultEntry entry, VaultAuditLog.Action action, AuditContext auditContext) {
        VaultAuditLog logEntry = new VaultAuditLog();
        logEntry.setVaultEntryId(entry.getId());
        logEntry.setMerchantId(entry.getMerchantId());
        logEntry.setAction(action);
        if (auditContext != null) {
            logEntry.setActorId(auditContext.actorId());
            logEntry.setIpAddress(auditContext.ipAddress());
            logEntry.setUserAgent(auditContext.userAgent());
        }
        vaultAuditLogRepository.save(logEntry);
    }

    private TokenizedPaymentMethod toTokenizedPaymentMethod(VaultEntry entry) {
        return new TokenizedPaymentMethod(
                entry.getToken(),
                entry.getEntryType(),
                entry.getBrand(),
                entry.getLast4(),
                entry.getCustomerId(),
                entry.getStatus());
    }

    private void validateCard(VaultEncryptionService.CardData card) {
        if (card == null || isBlank(card.number()) || isBlank(card.expMonth()) || isBlank(card.expYear())) {
            throw new BusinessException("Card number and expiry are required", HttpStatus.BAD_REQUEST);
        }

        String number = digitsOnly(card.number());
        if (number.length() < 12 || number.length() > 19 || !luhn(number)) {
            throw new BusinessException("Invalid card number", HttpStatus.BAD_REQUEST);
        }

        int month = parseNumber(card.expMonth(), "Invalid card expiry month");
        int year = parseNumber(card.expYear(), "Invalid card expiry year");
        if (year < 100) {
            year += 2000;
        }
        if (month < 1 || month > 12 || YearMonth.of(year, month).isBefore(YearMonth.now())) {
            throw new BusinessException("Card is expired", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateBankAccount(VaultEncryptionService.BankAccountData bank) {
        if (bank == null || isBlank(bank.accountNumber())) {
            throw new BusinessException("Bank account number is required", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateWallet(VaultEncryptionService.WalletData wallet) {
        if (wallet == null || isBlank(wallet.walletType()) || isBlank(wallet.walletId())) {
            throw new BusinessException("Wallet type and wallet id are required", HttpStatus.BAD_REQUEST);
        }
    }

    private int parseNumber(String value, String errorMessage) {
        try {
            return Integer.parseInt(digitsOnly(value));
        } catch (NumberFormatException e) {
            throw new BusinessException(errorMessage, HttpStatus.BAD_REQUEST);
        }
    }

    private String digitsOnly(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean luhn(String value) {
        int sum = 0;
        boolean doubleDigit = false;
        for (int i = value.length() - 1; i >= 0; i--) {
            int digit = Character.digit(value.charAt(i), 10);
            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleDigit = !doubleDigit;
        }
        return sum % 10 == 0;
    }

    public record AuditContext(UUID actorId, String ipAddress, String userAgent) {}

    public record TokenizedPaymentMethod(String token,
                                         VaultEntry.EntryType type,
                                         String brand,
                                         String last4,
                                         UUID customerId,
                                         VaultEntry.EntryStatus status) {}
}
