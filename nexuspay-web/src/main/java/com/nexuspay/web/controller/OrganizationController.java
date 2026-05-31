package com.nexuspay.web.controller;

import com.nexuspay.domain.entity.Merchant;
import com.nexuspay.domain.entity.Organization;
import com.nexuspay.domain.entity.OrganizationUser;
import com.nexuspay.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 运营管理端 API
 * 用于管理组织、商户审核、全局监控
 */
@RestController
@RequestMapping("/api/v1/admin/organizations")
@RequiredArgsConstructor
public class OrganizationController {
    
    private final OrganizationService organizationService;
    
    // Organization CRUD
    @PostMapping
    public ResponseEntity<Organization> create(@RequestBody OrganizationService.CreateRequest req) {
        return ResponseEntity.ok(organizationService.create(req));
    }
    
    @GetMapping
    public ResponseEntity<List<Organization>> listAll() {
        return ResponseEntity.ok(organizationService.listAll());
    }
    
    @GetMapping("/{orgId}")
    public ResponseEntity<Organization> get(@PathVariable UUID orgId) {
        return ResponseEntity.ok(organizationService.get(orgId));
    }
    
    @PutMapping("/{orgId}")
    public ResponseEntity<Organization> update(
            @PathVariable UUID orgId,
            @RequestBody OrganizationService.UpdateRequest req) {
        return ResponseEntity.ok(organizationService.update(orgId, req));
    }
    
    @DeleteMapping("/{orgId}")
    public ResponseEntity<Void> delete(@PathVariable UUID orgId) {
        organizationService.delete(orgId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{orgId}/stats")
    public ResponseEntity<OrganizationService.OrgStats> getStats(@PathVariable UUID orgId) {
        return ResponseEntity.ok(organizationService.getStats(orgId));
    }
    
    // Organization Users
    @PostMapping("/{orgId}/users")
    public ResponseEntity<OrganizationUser> addUser(
            @PathVariable UUID orgId,
            @RequestBody OrganizationService.AddUserRequest req) {
        return ResponseEntity.ok(organizationService.addUser(orgId, req));
    }
    
    @GetMapping("/{orgId}/users")
    public ResponseEntity<List<OrganizationUser>> listUsers(@PathVariable UUID orgId) {
        return ResponseEntity.ok(organizationService.listUsers(orgId));
    }
    
    @DeleteMapping("/{orgId}/users/{userId}")
    public ResponseEntity<Void> removeUser(
            @PathVariable UUID orgId,
            @PathVariable UUID userId) {
        organizationService.removeUser(orgId, userId);
        return ResponseEntity.noContent().build();
    }
    
    // Merchants under organization
    @PostMapping("/{orgId}/merchants")
    public ResponseEntity<Merchant> createMerchant(
            @PathVariable UUID orgId,
            @RequestBody OrganizationService.CreateMerchantRequest req) {
        return ResponseEntity.ok(organizationService.createMerchant(orgId, req));
    }
    
    @GetMapping("/{orgId}/merchants")
    public ResponseEntity<List<Merchant>> listMerchants(@PathVariable UUID orgId) {
        return ResponseEntity.ok(organizationService.listMerchants(orgId));
    }
    
    @PostMapping("/{orgId}/merchants/{merchantId}/activate")
    public ResponseEntity<Merchant> activateMerchant(
            @PathVariable UUID orgId,
            @PathVariable UUID merchantId) {
        return ResponseEntity.ok(organizationService.updateMerchantStatus(orgId, merchantId, Merchant.MerchantStatus.ACTIVE));
    }
    
    @PostMapping("/{orgId}/merchants/{merchantId}/suspend")
    public ResponseEntity<Merchant> suspendMerchant(
            @PathVariable UUID orgId,
            @PathVariable UUID merchantId) {
        return ResponseEntity.ok(organizationService.updateMerchantStatus(orgId, merchantId, Merchant.MerchantStatus.SUSPENDED));
    }
}
