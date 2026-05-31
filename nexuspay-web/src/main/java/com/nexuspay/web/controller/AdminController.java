package com.nexuspay.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 运营管理端 - 全局管理 API
 * 用于系统监控、全局数据统计、商户审核等
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    
    // 全局概览
    @GetMapping("/overview")
    public ResponseEntity<OverviewStats> getOverview() {
        // TODO: 实际查询数据库
        return ResponseEntity.ok(new OverviewStats(
                156,  // totalOrganizations
                1243, // totalMerchants
                45678, // totalPayments
                1234567L, // totalVolume
                98.5, // successRate
                12    // pendingApprovals
        ));
    }
    
    // 待审核商户列表
    @GetMapping("/pending-approvals")
    public ResponseEntity<List<PendingApproval>> getPendingApprovals() {
        return ResponseEntity.ok(List.of(
                new PendingApproval(UUID.randomUUID(), "新商户A", "merchant-a@example.com", "2026-05-30"),
                new PendingApproval(UUID.randomUUID(), "新商户B", "merchant-b@example.com", "2026-05-29")
        ));
    }
    
    // 商户审核
    @PostMapping("/merchants/{merchantId}/approve")
    public ResponseEntity<Map<String, String>> approveMerchant(@PathVariable UUID merchantId) {
        // TODO: 更新商户状态
        return ResponseEntity.ok(Map.of("status", "approved", "merchantId", merchantId.toString()));
    }
    
    @PostMapping("/merchants/{merchantId}/reject")
    public ResponseEntity<Map<String, String>> rejectMerchant(
            @PathVariable UUID merchantId,
            @RequestBody Map<String, String> body) {
        // TODO: 更新商户状态，记录拒绝原因
        return ResponseEntity.ok(Map.of("status", "rejected", "reason", body.get("reason")));
    }
    
    // 全局监控
    @GetMapping("/monitoring")
    public ResponseEntity<MonitoringData> getMonitoring() {
        return ResponseEntity.ok(new MonitoringData(
                List.of(
                        new ProviderHealth("STRIPE", "healthy", 99.9, 150),
                        new ProviderHealth("SQUARE", "healthy", 99.8, 80),
                        new ProviderHealth("BRAINTREE", "degraded", 98.5, 45)
                ),
                List.of(
                        new RecentError("ERR001", "Connection timeout", "STRIPE", "2026-05-31T11:30:00Z"),
                        new RecentError("ERR002", "Invalid card", "SQUARE", "2026-05-31T11:25:00Z")
                ),
                new SystemMetrics(45.2, 62.8, 1024)
        ));
    }
    
    // 支付方式配置
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
    
    // Records
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
