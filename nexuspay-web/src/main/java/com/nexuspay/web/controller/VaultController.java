package com.nexuspay.web.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nexuspay.domain.entity.VaultAuditLog;
import com.nexuspay.domain.entity.VaultEntry;
import com.nexuspay.service.VaultEncryptionService;
import com.nexuspay.service.VaultService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vault")
@RequiredArgsConstructor
public class VaultController {

    private final VaultService vaultService;

    @PostMapping("/cards")
    public ResponseEntity<VaultService.TokenizedPaymentMethod> storeCard(
            @RequestAttribute("merchantId") UUID merchantId,
            @RequestAttribute(value = "userId", required = false) UUID userId,
            @Valid @RequestBody StoreCardRequest req,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(vaultService.storeCard(
                merchantId,
                req.customerId(),
                new VaultEncryptionService.CardData(
                        req.number(),
                        coalesce(req.expMonth(), req.exp_month()),
                        coalesce(req.expYear(), req.exp_year()),
                        req.cvc(),
                        req.brand(),
                        null,
                        coalesce(req.cardholderName(), req.cardholder_name())),
                auditContext(userId, servletRequest)));
    }

    @PostMapping("/bank-accounts")
    public ResponseEntity<VaultService.TokenizedPaymentMethod> storeBankAccount(
            @RequestAttribute("merchantId") UUID merchantId,
            @RequestAttribute(value = "userId", required = false) UUID userId,
            @Valid @RequestBody StoreBankAccountRequest req,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(vaultService.storeBankAccount(
                merchantId,
                req.customerId(),
                new VaultEncryptionService.BankAccountData(
                        req.accountNumber(),
                        req.routingNumber(),
                        req.bankName(),
                        req.accountHolderName(),
                        req.accountType()),
                auditContext(userId, servletRequest)));
    }

    @PostMapping("/wallets")
    public ResponseEntity<VaultService.TokenizedPaymentMethod> storeWallet(
            @RequestAttribute("merchantId") UUID merchantId,
            @RequestAttribute(value = "userId", required = false) UUID userId,
            @Valid @RequestBody StoreWalletRequest req,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(vaultService.storeWallet(
                merchantId,
                req.customerId(),
                new VaultEncryptionService.WalletData(req.walletType(), req.walletId(), req.email()),
                auditContext(userId, servletRequest)));
    }

    @GetMapping
    public ResponseEntity<List<VaultEntrySummary>> list(
            @RequestAttribute("merchantId") UUID merchantId,
            @RequestParam(required = false) UUID customerId) {
        List<VaultEntry> entries = customerId != null
                ? vaultService.listByCustomer(merchantId, customerId)
                : vaultService.listByMerchant(merchantId);
        return ResponseEntity.ok(entries.stream().map(VaultEntrySummary::from).toList());
    }

    @PostMapping("/detokenize")
    public ResponseEntity<?> detokenize(
            @RequestAttribute("merchantId") UUID merchantId,
            @RequestAttribute(value = "userId", required = false) UUID userId,
            @Valid @RequestBody TokenRequest req,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(vaultService.retrieve(merchantId, req.token(), auditContext(userId, servletRequest)));
    }

    @PostMapping("/revoke")
    public ResponseEntity<Void> revoke(
            @RequestAttribute("merchantId") UUID merchantId,
            @RequestAttribute(value = "userId", required = false) UUID userId,
            @Valid @RequestBody TokenRequest req,
            HttpServletRequest servletRequest) {
        vaultService.revoke(merchantId, req.token(), auditContext(userId, servletRequest));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<VaultAuditLog>> auditLogs(@RequestAttribute("merchantId") UUID merchantId) {
        return ResponseEntity.ok(vaultService.listAuditLogs(merchantId));
    }

    private VaultService.AuditContext auditContext(UUID userId, HttpServletRequest request) {
        return new VaultService.AuditContext(userId, request.getRemoteAddr(), request.getHeader("User-Agent"));
    }

    private String coalesce(String first, String second) {
        return first != null ? first : second;
    }

    public record StoreCardRequest(
            UUID customerId,
            @NotBlank String number,
            String expMonth,
            String expYear,
            String cvc,
            String brand,
            String cardholderName,
            @JsonProperty("exp_month") String exp_month,
            @JsonProperty("exp_year") String exp_year,
            @JsonProperty("cardholder_name") String cardholder_name) {}

    public record StoreBankAccountRequest(
            UUID customerId,
            @NotBlank String accountNumber,
            String routingNumber,
            String bankName,
            String accountHolderName,
            String accountType) {}

    public record StoreWalletRequest(
            UUID customerId,
            @NotBlank String walletType,
            @NotBlank String walletId,
            String email) {}

    public record TokenRequest(@NotBlank String token) {}

    public record VaultEntrySummary(
            UUID id,
            String token,
            VaultEntry.EntryType type,
            String brand,
            String last4,
            UUID customerId,
            VaultEntry.EntryStatus status,
            Instant lastUsedAt,
            Instant createdAt,
            Map<String, Object> metadata) {
        static VaultEntrySummary from(VaultEntry entry) {
            return new VaultEntrySummary(
                    entry.getId(),
                    entry.getToken(),
                    entry.getEntryType(),
                    entry.getBrand(),
                    entry.getLast4(),
                    entry.getCustomerId(),
                    entry.getStatus(),
                    entry.getLastUsedAt(),
                    entry.getCreatedAt(),
                    null);
        }
    }
}
