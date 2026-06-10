package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.domain.entity.MerchantUser;
import com.nexuspay.domain.entity.UserRole;
import com.nexuspay.repository.MerchantUserRepository;
import com.nexuspay.repository.RoleRepository;
import com.nexuspay.repository.UserRoleRepository;
import com.nexuspay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService {
    
    private final MerchantUserRepository merchantUserRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    
    public List<MerchantUser> listMembers(UUID merchantId) {
        return merchantUserRepository.findByMerchantId(merchantId);
    }
    
    @Transactional
    public MerchantUser updateRole(UUID merchantId, UUID userId, MerchantUser.Role newRole) {
        MerchantUser member = merchantUserRepository.findByUserIdAndMerchantId(userId, merchantId)
                .orElseThrow(() -> new BusinessException("Member not found", HttpStatus.NOT_FOUND));
        
        if (member.getRole() == MerchantUser.Role.OWNER) {
            throw new BusinessException("Cannot change owner role", HttpStatus.BAD_REQUEST);
        }
        
        member.setRole(newRole);
        MerchantUser saved = merchantUserRepository.save(member);
        syncMerchantRoleGrant(userId, merchantId, newRole);
        return saved;
    }
    
    @Transactional
    public void removeMember(UUID merchantId, UUID userId) {
        MerchantUser member = merchantUserRepository.findByUserIdAndMerchantId(userId, merchantId)
                .orElseThrow(() -> new BusinessException("Member not found", HttpStatus.NOT_FOUND));
        
        if (member.getRole() == MerchantUser.Role.OWNER) {
            throw new BusinessException("Cannot remove owner", HttpStatus.BAD_REQUEST);
        }
        
        merchantUserRepository.delete(member);
        userRoleRepository.deleteByUserIdAndScopeTypeAndScopeId(userId, "MERCHANT", merchantId);
    }
    
    @Transactional
    public MerchantUser inviteMember(UUID merchantId, String email, MerchantUser.Role role, UUID invitedBy) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));
        
        if (merchantUserRepository.existsByUserIdAndMerchantId(user.getId(), merchantId)) {
            throw new BusinessException("User already a member", HttpStatus.CONFLICT);
        }
        
        MerchantUser member = new MerchantUser();
        member.setUserId(user.getId());
        member.setMerchantId(merchantId);
        member.setRole(role);
        member.setInvitedBy(invitedBy);
        member.setStatus(MerchantUser.MemberStatus.PENDING_INVITE);
        
        return merchantUserRepository.save(member);
    }

    private void syncMerchantRoleGrant(UUID userId, UUID merchantId, MerchantUser.Role role) {
        userRoleRepository.deleteByUserIdAndScopeTypeAndScopeId(userId, "MERCHANT", merchantId);

        String roleCode = switch (role) {
            case OWNER -> "MERCHANT_OWNER";
            case ADMIN -> "MERCHANT_ADMIN";
            case DEVELOPER -> "MERCHANT_DEVELOPER";
            case FINANCE -> "MERCHANT_FINANCE";
            case VIEWER -> "MERCHANT_VIEWER";
        };

        var configuredRole = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new BusinessException("Merchant role not configured", HttpStatus.INTERNAL_SERVER_ERROR));

        UserRole grant = new UserRole();
        grant.setUserId(userId);
        grant.setRoleId(configuredRole.getId());
        grant.setScopeType("MERCHANT");
        grant.setScopeId(merchantId);
        userRoleRepository.save(grant);
    }
}
