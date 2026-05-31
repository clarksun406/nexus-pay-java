package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.domain.entity.Merchant;
import com.nexuspay.domain.entity.Organization;
import com.nexuspay.domain.entity.OrganizationUser;
import com.nexuspay.repository.MerchantRepository;
import com.nexuspay.repository.OrganizationRepository;
import com.nexuspay.repository.OrganizationUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationService {
    
    private final OrganizationRepository organizationRepository;
    private final OrganizationUserRepository organizationUserRepository;
    private final MerchantRepository merchantRepository;
    
    @Transactional
    public Organization create(CreateRequest req) {
        Organization org = new Organization();
        org.setName(req.name());
        return organizationRepository.save(org);
    }
    
    public Organization get(UUID orgId) {
        return organizationRepository.findById(orgId)
                .orElseThrow(() -> new BusinessException("Organization not found", HttpStatus.NOT_FOUND));
    }
    
    public List<Organization> listAll() {
        return organizationRepository.findAll();
    }
    
    @Transactional
    public Organization update(UUID orgId, UpdateRequest req) {
        Organization org = get(orgId);
        if (req.name() != null) org.setName(req.name());
        if (req.status() != null) org.setStatus(req.status());
        return organizationRepository.save(org);
    }
    
    @Transactional
    public void delete(UUID orgId) {
        Organization org = get(orgId);
        org.setStatus(Organization.OrgStatus.INACTIVE);
        organizationRepository.save(org);
    }
    
    // Organization User management
    @Transactional
    public OrganizationUser addUser(UUID orgId, AddUserRequest req) {
        Organization org = get(orgId);
        
        OrganizationUser user = new OrganizationUser();
        user.setOrganizationId(orgId);
        user.setUserId(req.userId());
        user.setRole(req.role());
        user.setStatus(OrganizationUser.OrgUserStatus.ACTIVE);
        
        return organizationUserRepository.save(user);
    }
    
    public List<OrganizationUser> listUsers(UUID orgId) {
        return organizationUserRepository.findByOrganizationId(orgId);
    }
    
    @Transactional
    public void removeUser(UUID orgId, UUID userId) {
        OrganizationUser user = organizationUserRepository.findByOrganizationIdAndUserId(orgId, userId)
                .orElseThrow(() -> new BusinessException("User not found in organization", HttpStatus.NOT_FOUND));
        user.setStatus(OrganizationUser.OrgUserStatus.INACTIVE);
        organizationUserRepository.save(user);
    }
    
    // Merchant management under organization
    @Transactional
    public Merchant createMerchant(UUID orgId, CreateMerchantRequest req) {
        Organization org = get(orgId);
        
        Merchant merchant = new Merchant();
        merchant.setOrganizationId(orgId);
        merchant.setName(req.name());
        
        return merchantRepository.save(merchant);
    }
    
    public List<Merchant> listMerchants(UUID orgId) {
        return merchantRepository.findByOrganizationId(orgId);
    }
    
    @Transactional
    public Merchant updateMerchantStatus(UUID orgId, UUID merchantId, Merchant.MerchantStatus status) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new BusinessException("Merchant not found", HttpStatus.NOT_FOUND));
        
        if (!merchant.getOrganizationId().equals(orgId)) {
            throw new BusinessException("Merchant not in organization", HttpStatus.FORBIDDEN);
        }
        
        merchant.setStatus(status);
        return merchantRepository.save(merchant);
    }
    
    // Statistics
    public OrgStats getStats(UUID orgId) {
        List<Merchant> merchants = merchantRepository.findByOrganizationId(orgId);
        int activeMerchants = (int) merchants.stream().filter(m -> m.getStatus() == Merchant.MerchantStatus.ACTIVE).count();
        int suspendedMerchants = (int) merchants.stream().filter(m -> m.getStatus() == Merchant.MerchantStatus.SUSPENDED).count();
        
        return new OrgStats(merchants.size(), activeMerchants, suspendedMerchants);
    }
    
    public record CreateRequest(String name) {}
    public record UpdateRequest(String name, Organization.OrgStatus status) {}
    public record AddUserRequest(UUID userId, OrganizationUser.OrgRole role) {}
    public record CreateMerchantRequest(String name) {}
    public record OrgStats(int totalMerchants, int activeMerchants, int suspendedMerchants) {}
}
