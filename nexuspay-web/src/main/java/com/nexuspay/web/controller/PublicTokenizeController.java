package com.nexuspay.web.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nexuspay.domain.entity.ApiKey;
import com.nexuspay.repository.ApiKeyRepository;
import com.nexuspay.service.VaultEncryptionService;
import com.nexuspay.service.VaultService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/pub")
@RequiredArgsConstructor
public class PublicTokenizeController {

    private final ApiKeyRepository apiKeyRepository;
    private final VaultService vaultService;

    @PostMapping({"/tokenize", "/elements/tokenize"})
    public ResponseEntity<?> tokenize(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody TokenizeRequest req,
            HttpServletRequest servletRequest) {
        ApiKey key = authenticatePublishableKey(authHeader);
        if (key == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid publishable key"));
        }

        VaultService.AuditContext auditContext = publicAuditContext(servletRequest);
        VaultService.TokenizedPaymentMethod tokenized = tokenizeRequest(key.getMerchantId(), req, auditContext);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("token", tokenized.token());
        response.put("merchantId", key.getMerchantId());
        response.put("type", tokenized.type());
        response.put("brand", tokenized.brand());
        response.put("last4", tokenized.last4());
        response.put("provider", req.provider());
        response.put("providerPaymentMethod", req.providerPaymentMethod());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/providers")
    public ResponseEntity<?> getProviders(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authenticatePublishableKey(authHeader) == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(Map.of(
                "providers", java.util.List.of(
                    Map.of("name", "STRIPE", "publishableKey", "pk_test_xxx"),
                    Map.of("name", "SQUARE", "applicationId", "sandbox-xxx"),
                    Map.of("name", "BRAINTREE", "clientToken", "xxx")
                )
        ));
    }

    private VaultService.TokenizedPaymentMethod tokenizeRequest(
            UUID merchantId,
            TokenizeRequest req,
            VaultService.AuditContext auditContext) {
        UUID customerId = req.customerId();
        if (req.card() != null) {
            TokenizeCardRequest card = req.card();
            return vaultService.storeCard(merchantId, customerId,
                    new VaultEncryptionService.CardData(
                            card.number(),
                            coalesce(card.expMonth(), card.exp_month()),
                            coalesce(card.expYear(), card.exp_year()),
                            card.cvc(),
                            coalesce(card.brand(), req.brand()),
                            null,
                            coalesce(card.cardholderName(), card.cardholder_name())),
                    auditContext);
        }
        if (req.bankAccount() != null) {
            TokenizeBankAccountRequest bank = req.bankAccount();
            return vaultService.storeBankAccount(merchantId, customerId,
                    new VaultEncryptionService.BankAccountData(
                            bank.accountNumber(),
                            bank.routingNumber(),
                            bank.bankName(),
                            bank.accountHolderName(),
                            bank.accountType()),
                    auditContext);
        }
        if (req.wallet() != null) {
            TokenizeWalletRequest wallet = req.wallet();
            return vaultService.storeWallet(merchantId, customerId,
                    new VaultEncryptionService.WalletData(wallet.walletType(), wallet.walletId(), wallet.email()),
                    auditContext);
        }

        return vaultService.storeWallet(merchantId, customerId,
                new VaultEncryptionService.WalletData(
                        req.provider() != null ? req.provider() : "gateway",
                        req.providerPaymentMethod(),
                        null),
                auditContext);
    }

    private ApiKey authenticatePublishableKey(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("pk_")) {
            return null;
        }

        return apiKeyRepository.findByKeyHash(hashKey(authHeader))
                .filter(k -> k.getType() == ApiKey.KeyType.PUBLISHABLE)
                .filter(k -> k.getStatus() == ApiKey.KeyStatus.ACTIVE)
                .orElse(null);
    }

    private VaultService.AuditContext publicAuditContext(HttpServletRequest request) {
        return new VaultService.AuditContext(
                null,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));
    }

    private String hashKey(String key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(key.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash API key", e);
        }
    }

    private String coalesce(String first, String second) {
        return first != null ? first : second;
    }

    public record TokenizeRequest(
            String provider,
            String providerPaymentMethod,
            UUID customerId,
            String brand,
            TokenizeCardRequest card,
            TokenizeBankAccountRequest bankAccount,
            TokenizeWalletRequest wallet) {}

    public record TokenizeCardRequest(
            String number,
            String expMonth,
            String expYear,
            String cvc,
            String brand,
            String cardholderName,
            @JsonProperty("exp_month") String exp_month,
            @JsonProperty("exp_year") String exp_year,
            @JsonProperty("cardholder_name") String cardholder_name) {}

    public record TokenizeBankAccountRequest(
            String accountNumber,
            String routingNumber,
            String bankName,
            String accountHolderName,
            String accountType) {}

    public record TokenizeWalletRequest(
            String walletType,
            String walletId,
            String email) {}
}
