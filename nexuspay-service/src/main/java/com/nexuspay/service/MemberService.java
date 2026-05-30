package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.domain.entity.MerchantUser;
import com.nexuspay.repository.MerchantUserRepository;
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
        return merchantUserRepository.save(member);
    }
    
    @Transactional
    public void removeMember(UUID merchantId, UUID userId) {
        MerchantUser member = merchantUserRepository.findByUserIdAndMerchantId(userId, merchantId)
                .orElseThrow(() -> new BusinessException("Member not found", HttpStatus.NOT_FOUND));
        
        if (member.getRole() == MerchantUser.Role.OWNER) {
            throw new BusinessException("Cannot remove owner", HttpStatus.BAD_REQUEST);
        }
        
        merchantUserRepository.delete(member);
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
}
