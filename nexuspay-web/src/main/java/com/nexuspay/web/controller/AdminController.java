package com.nexuspay.web.controller;

import com.nexuspay.domain.entity.Merchant;
import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.domain.entity.ProviderAccount;
import com.nexuspay.repository.MerchantRepository;
import com.nexuspay.repository.OrganizationRepository;
import com.nexuspay.repository.PaymentIntentRepository;
import com.nexuspay.repository.ProviderAccountRepository;
import com.nexuspay.service.RequirePermission;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final OrganizationRepository organizationRepository;
    private final MerchantRepository merchantRepository;
    private final PaymentIntentRepository paymentIntentRepository;
    private final ProviderAccountRepository providerAccountRepository;

    @GetMapping("/overview")
    @RequirePermission("SYSTEM_MONITOR")
    public ResponseEntity<OverviewStats> getOverview() {
        List<PaymentIntent> intents = paymentIntentRepository.findAll();
        long succeeded = intents.stream()
                .filter(intent -> intent.getStatus() == PaymentIntent.PaymentStatus.SUCCEEDED)
                .count();
        long totalVolume = intents.stream()
                .filter(intent -> intent.getStatus() == PaymentIntent.PaymentStatus.SUCCEEDED)
                .map(PaymentIntent::getAmount)
                .reduce(BigInteger.ZERO, BigInteger::add)
                .longValue();
        double successRate = intents.isEmpty() ? 0.0 : (double) succeeded / intents.size() * 100.0;
        int pendingApprovals = (int) merchantRepository.findAll().stream()
                .filter(merchant -> merchant.getStatus() == Merchant.MerchantStatus.INACTIVE)
                .count();

        return ResponseEntity.ok(new OverviewStats(
                Math.toIntExact(organizationRepository.count()),
                Math.toIntExact(merchantRepository.count()),
                intents.size(),
                totalVolume,
                successRate,
                pendingApprovals
        ));
    }

    @GetMapping("/pending-approvals")
    public ResponseEntity<List<PendingApproval>> getPendingApprovals() {
        List<PendingApproval> pending = merchantRepository.findAll().stream()
                .filter(merchant -> merchant.getStatus() == Merchant.MerchantStatus.INACTIVE)
                .map(merchant -> new PendingApproval(
                        merchant.getId(),
                        merchant.getName(),
                        "",
                        merchant.getCreatedAt() != null ? merchant.getCreatedAt().toString() : ""
                ))
                .toList();
        return ResponseEntity.ok(pending);
    }

    @PostMapping("/merchants/{merchantId}/approve")
    @RequirePermission("MERCHANT_APPROVE")
    public ResponseEntity<Map<String, String>> approveMerchant(@PathVariable UUID merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new NoSuchElementException("Merchant not found"));
        merchant.setStatus(Merchant.MerchantStatus.ACTIVE);
        merchantRepository.save(merchant);
        return ResponseEntity.ok(Map.of("status", "approved", "merchantId", merchantId.toString()));
    }

    @PostMapping("/merchants/{merchantId}/reject")
    @RequirePermission("MERCHANT_SUSPEND")
    public ResponseEntity<Map<String, String>> rejectMerchant(
            @PathVariable UUID merchantId,
            @RequestBody Map<String, String> body) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new NoSuchElementException("Merchant not found"));
        merchant.setStatus(Merchant.MerchantStatus.SUSPENDED);
        merchantRepository.save(merchant);
        return ResponseEntity.ok(Map.of(
                "status", "rejected",
                "merchantId", merchantId.toString(),
                "reason", body.getOrDefault("reason", "")
        ));
    }

    @GetMapping("/monitoring")
    @RequirePermission("SYSTEM_MONITOR")
    public ResponseEntity<MonitoringData> getMonitoring() {
        Map<ProviderAccount.Provider, List<ProviderAccount>> byProvider = providerAccountRepository.findAll().stream()
                .collect(Collectors.groupingBy(ProviderAccount::getProvider));

        List<ProviderHealth> providers = Arrays.stream(ProviderAccount.Provider.values())
                .map(provider -> {
                    List<ProviderAccount> accounts = byProvider.getOrDefault(provider, List.of());
                    long unhealthy = accounts.stream()
                            .filter(account -> account.getStatus() == ProviderAccount.ConnectorStatus.UNHEALTHY)
                            .count();
                    String status = accounts.isEmpty() ? "not_configured" : unhealthy > 0 ? "degraded" : "healthy";
                    double uptime = accounts.isEmpty()
                            ? 0.0
                            : (double) (accounts.size() - unhealthy) / accounts.size() * 100.0;
                    return new ProviderHealth(provider.name(), status, uptime, accounts.size());
                })
                .toList();

        List<RecentError> recentErrors = paymentIntentRepository.findByStatus(PaymentIntent.PaymentStatus.FAILED)
                .stream()
                .limit(10)
                .map(intent -> new RecentError(
                        intent.getId().toString(),
                        "Payment failed",
                        intent.getResolvedProvider() != null ? intent.getResolvedProvider().name() : "",
                        intent.getUpdatedAt() != null ? intent.getUpdatedAt().toString() : Instant.now().toString()
                ))
                .toList();

        return ResponseEntity.ok(new MonitoringData(
                providers,
                recentErrors,
                new SystemMetrics(0.0, 0.0, 0)
        ));
    }

    @GetMapping("/payment-methods")
    public ResponseEntity<List<PaymentMethodConfig>> getPaymentMethods() {
        return ResponseEntity.ok(List.of(
                new PaymentMethodConfig("card", true, List.of("VISA", "MASTERCARD", "AMEX")),
                new PaymentMethodConfig("alipay", true, List.of("CN")),
                new PaymentMethodConfig("wechat_pay", true, List.of("CN")),
                new PaymentMethodConfig("apple_pay", true, List.of("GLOBAL")),
                new PaymentMethodConfig("google_pay", true, List.of("GLOBAL"))
        ));
    }

    @PutMapping("/payment-methods/{method}")
    public ResponseEntity<Map<String, String>> updatePaymentMethod(
            @PathVariable String method,
            @RequestBody Map<String, Object> config) {
        return ResponseEntity.ok(Map.of("status", "updated", "method", method));
    }

    public record OverviewStats(
            int totalOrganizations,
            int totalMerchants,
            int totalPayments,
            long totalVolume,
            double successRate,
            int pendingApprovals
    ) {}

    public record PendingApproval(UUID id, String name, String email, String submittedAt) {}

    public record MonitoringData(
            List<ProviderHealth> providers,
            List<RecentError> recentErrors,
            SystemMetrics system
    ) {}

    public record ProviderHealth(String provider, String status, double uptime, int requestsPerMin) {}

    public record RecentError(String code, String message, String provider, String timestamp) {}

    public record SystemMetrics(double cpuUsage, double memoryUsage, int activeConnections) {}

    public record PaymentMethodConfig(String method, boolean enabled, List<String> supportedRegions) {}
}
