package com.nexuspay.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexuspay.common.util.CryptoUtil;
import com.nexuspay.domain.entity.VaultEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VaultEncryptionService {

    private static final String TOKEN_PREFIX = "vault_";
    private static final int TOKEN_BYTES = 32;
    private static final int IV_SIZE = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final String CIPHERTEXT_PREFIX = "jwe:";
    private static final String ENCRYPTION_ALGORITHM = "JWE_A256GCM";
    private static final String KEY_MODEL = "MASTER_CUSTODIAN";

    private final CryptoUtil cryptoUtil;
    private final ObjectMapper objectMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${security.vault.master-key:${security.encryption.key:change-this-vault-master-key}}")
    private String masterKeyMaterial;

    @Value("${security.vault.custodian-key:${security.encryption.key:change-this-vault-custodian-key}}")
    private String custodianKeyMaterial;

    @Value("${security.vault.key-id:local-v1}")
    private String keyId;

    public record CardData(String number, String expMonth, String expYear, String cvc,
                           String brand, String last4, String cardholderName) {}

    public record BankAccountData(String accountNumber, String routingNumber,
                                  String bankName, String accountHolderName, String accountType) {}

    public record WalletData(String walletType, String walletId, String email) {}

    public record TokenizedResult(String token, String fingerprint, String last4,
                                  String brand, VaultEntry.EntryType type, String encryptedData,
                                  String encryptedDataKey, String keyId, String encryptionAlgorithm,
                                  String keyModel, String signature) {}

    public record DetokenizedResult(VaultEntry.EntryType type, Map<String, Object> data) {}

    private record StoredCardData(String number, String expMonth, String expYear,
                                  String brand, String last4, String cardholderName) {}

    private record EncryptionEnvelope(String encryptedData, String encryptedDataKey) {}

    public TokenizedResult tokenizeCard(UUID merchantId, UUID customerId, CardData card) {
        StoredCardData stored = new StoredCardData(
                digitsOnly(card.number()),
                normalizeMonth(card.expMonth()),
                normalizeYear(card.expYear()),
                normalizeBrand(card.brand()),
                resolveLast4(card.number(), card.last4()),
                card.cardholderName());

        String json = toJson(stored);
        String fingerprint = fingerprint(merchantId, "card:" + stored.number());
        EncryptionEnvelope envelope = encryptWithEnvelope(json);
        String signature = signPayload(envelope.encryptedData(), envelope.encryptedDataKey(), keyId);
        String token = generateToken();

        return new TokenizedResult(token, fingerprint, stored.last4(), stored.brand(),
                VaultEntry.EntryType.CARD, envelope.encryptedData(), envelope.encryptedDataKey(),
                keyId, ENCRYPTION_ALGORITHM, KEY_MODEL, signature);
    }

    public TokenizedResult tokenizeBankAccount(UUID merchantId, UUID customerId, BankAccountData bank) {
        String accountNumber = digitsOnly(bank.accountNumber());
        String routingNumber = digitsOnly(bank.routingNumber());
        String json = toJson(bank);
        String fingerprint = fingerprint(merchantId, "bank:" + routingNumber + ":" + accountNumber);
        EncryptionEnvelope envelope = encryptWithEnvelope(json);
        String signature = signPayload(envelope.encryptedData(), envelope.encryptedDataKey(), keyId);
        String token = generateToken();

        return new TokenizedResult(token, fingerprint, resolveLast4(accountNumber, null),
                bank.bankName(), VaultEntry.EntryType.BANK_ACCOUNT, envelope.encryptedData(),
                envelope.encryptedDataKey(), keyId, ENCRYPTION_ALGORITHM, KEY_MODEL, signature);
    }

    public TokenizedResult tokenizeWallet(UUID merchantId, UUID customerId, WalletData wallet) {
        String json = toJson(wallet);
        String fingerprint = fingerprint(merchantId, "wallet:" + wallet.walletType() + ":" + wallet.walletId());
        EncryptionEnvelope envelope = encryptWithEnvelope(json);
        String signature = signPayload(envelope.encryptedData(), envelope.encryptedDataKey(), keyId);
        String token = generateToken();

        return new TokenizedResult(token, fingerprint, null, wallet.walletType(),
                VaultEntry.EntryType.WALLET, envelope.encryptedData(), envelope.encryptedDataKey(),
                keyId, ENCRYPTION_ALGORITHM, KEY_MODEL, signature);
    }

    public DetokenizedResult detokenize(VaultEntry entry) {
        verifyIntegrity(entry);
        String json = decryptWithEnvelope(entry.getEncryptedData(), entry.getEncryptedDataKey());
        return new DetokenizedResult(entry.getEntryType(), parseJson(json));
    }

    public void verifyIntegrity(VaultEntry entry) {
        if (entry.getDataSignature() == null) {
            return;
        }
        String expected = signPayload(entry.getEncryptedData(), entry.getEncryptedDataKey(), entry.getKeyId());
        if (!constantTimeEquals(expected, entry.getDataSignature())) {
            throw new IllegalStateException("Vault data integrity check failed");
        }
    }

    public String signPayload(String encryptedData, String encryptedDataKey, String keyId) {
        return cryptoUtil.hashSha256(encryptedData + "." + encryptedDataKey + "." + keyId, signingSecret());
    }

    public String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return TOKEN_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String hashToken(String token) {
        return cryptoUtil.hashSha256(token, "vault_token");
    }

    private EncryptionEnvelope encryptWithEnvelope(String plaintext) {
        SecretKey dataKey = generateDataKey();
        String encryptedData = encryptAesGcm(plaintext.getBytes(StandardCharsets.UTF_8), dataKey);
        String encryptedDataKey = encryptAesGcm(dataKey.getEncoded(), wrappingKey());
        return new EncryptionEnvelope(encryptedData, encryptedDataKey);
    }

    private String decryptWithEnvelope(String encryptedData, String encryptedDataKey) {
        byte[] dataKeyBytes = decryptAesGcm(encryptedDataKey, wrappingKey());
        SecretKey dataKey = new SecretKeySpec(dataKeyBytes, "AES");
        return new String(decryptAesGcm(encryptedData, dataKey), StandardCharsets.UTF_8);
    }

    private SecretKey generateDataKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256, secureRandom);
            return keyGenerator.generateKey();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate vault data key", e);
        }
    }

    private String encryptAesGcm(byte[] plaintext, SecretKey key) {
        try {
            byte[] iv = new byte[IV_SIZE];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] encrypted = cipher.doFinal(plaintext);

            byte[] payload = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, payload, 0, iv.length);
            System.arraycopy(encrypted, 0, payload, iv.length, encrypted.length);

            return CIPHERTEXT_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(payload);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encrypt vault payload", e);
        }
    }

    private byte[] decryptAesGcm(String ciphertext, SecretKey key) {
        if (ciphertext == null || !ciphertext.startsWith(CIPHERTEXT_PREFIX)) {
            throw new IllegalArgumentException("Invalid vault ciphertext");
        }

        try {
            byte[] payload = Base64.getUrlDecoder().decode(ciphertext.substring(CIPHERTEXT_PREFIX.length()));
            if (payload.length <= IV_SIZE) {
                throw new IllegalArgumentException("Invalid vault ciphertext payload");
            }

            byte[] iv = Arrays.copyOfRange(payload, 0, IV_SIZE);
            byte[] encrypted = Arrays.copyOfRange(payload, IV_SIZE, payload.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return cipher.doFinal(encrypted);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to decrypt vault payload", e);
        }
    }

    private SecretKey wrappingKey() {
        return new SecretKeySpec(sha256("master:" + masterKey() + "|custodian:" + custodianKey()), "AES");
    }

    private String signingSecret() {
        return "vault_sig:" + masterKey() + ":" + custodianKey();
    }

    private String fingerprint(UUID merchantId, String normalizedValue) {
        return cryptoUtil.hashSha256(merchantId + ":" + normalizedValue, "vault_fp:" + masterKey());
    }

    private byte[] sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to derive vault key", e);
        }
    }

    private boolean constantTimeEquals(String left, String right) {
        return MessageDigest.isEqual(
                left.getBytes(StandardCharsets.UTF_8),
                right.getBytes(StandardCharsets.UTF_8));
    }

    private String masterKey() {
        return masterKeyMaterial == null || masterKeyMaterial.isBlank()
                ? "change-this-vault-master-key"
                : masterKeyMaterial;
    }

    private String custodianKey() {
        return custodianKeyMaterial == null || custodianKeyMaterial.isBlank()
                ? "change-this-vault-custodian-key"
                : custodianKeyMaterial;
    }

    private String digitsOnly(String value) {
        return value == null ? null : value.replaceAll("\\D", "");
    }

    private String normalizeMonth(String value) {
        String digits = digitsOnly(value);
        return digits != null && digits.length() == 1 ? "0" + digits : digits;
    }

    private String normalizeYear(String value) {
        String digits = digitsOnly(value);
        return digits != null && digits.length() == 2 ? "20" + digits : digits;
    }

    private String normalizeBrand(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private String resolveLast4(String number, String providedLast4) {
        if (providedLast4 != null && providedLast4.length() == 4) {
            return providedLast4;
        }
        String digits = digitsOnly(number);
        return digits != null && digits.length() >= 4 ? digits.substring(digits.length() - 4) : null;
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize vault data", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize vault data", e);
        }
    }
}
