package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.domain.entity.MerchantUser;
import com.nexuspay.domain.entity.User;
import com.nexuspay.repository.MerchantUserRepository;
import com.nexuspay.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberServiceTest {

    @Mock
    private MerchantUserRepository merchantUserRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MemberService memberService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldListMembersByMerchantId() {
        UUID merchantId = UUID.randomUUID();
        when(merchantUserRepository.findByMerchantId(merchantId))
                .thenReturn(List.of(new MerchantUser(), new MerchantUser()));

        List<MerchantUser> result = memberService.listMembers(merchantId);

        assertEquals(2, result.size());
        verify(merchantUserRepository).findByMerchantId(merchantId);
    }

    @Test
    void shouldUpdateRoleForNonOwnerMember() {
        UUID merchantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        MerchantUser member = new MerchantUser();
        member.setUserId(userId);
        member.setMerchantId(merchantId);
        member.setRole(MerchantUser.Role.DEVELOPER);

        when(merchantUserRepository.findByUserIdAndMerchantId(userId, merchantId))
                .thenReturn(Optional.of(member));
        when(merchantUserRepository.save(any(MerchantUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MerchantUser result = memberService.updateRole(merchantId, userId, MerchantUser.Role.ADMIN);

        assertEquals(MerchantUser.Role.ADMIN, result.getRole());
        verify(merchantUserRepository).save(member);
    }

    @Test
    void shouldThrowNotFoundWhenUpdateRoleMemberMissing() {
        UUID merchantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(merchantUserRepository.findByUserIdAndMerchantId(userId, merchantId))
                .thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> memberService.updateRole(merchantId, userId, MerchantUser.Role.ADMIN));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals("Member not found", ex.getMessage());
    }

    @Test
    void shouldRejectUpdateRoleForOwner() {
        UUID merchantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        MerchantUser owner = new MerchantUser();
        owner.setRole(MerchantUser.Role.OWNER);

        when(merchantUserRepository.findByUserIdAndMerchantId(userId, merchantId))
                .thenReturn(Optional.of(owner));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> memberService.updateRole(merchantId, userId, MerchantUser.Role.ADMIN));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("Cannot change owner role", ex.getMessage());
        verify(merchantUserRepository, never()).save(any());
    }

    @Test
    void shouldRemoveNonOwnerMember() {
        UUID merchantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        MerchantUser member = new MerchantUser();
        member.setRole(MerchantUser.Role.FINANCE);

        when(merchantUserRepository.findByUserIdAndMerchantId(userId, merchantId))
                .thenReturn(Optional.of(member));

        memberService.removeMember(merchantId, userId);

        verify(merchantUserRepository).delete(member);
    }

    @Test
    void shouldRejectRemoveOwner() {
        UUID merchantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        MerchantUser owner = new MerchantUser();
        owner.setRole(MerchantUser.Role.OWNER);

        when(merchantUserRepository.findByUserIdAndMerchantId(userId, merchantId))
                .thenReturn(Optional.of(owner));

        BusinessException ex = assertThrows(BusinessException.class, () -> memberService.removeMember(merchantId, userId));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("Cannot remove owner", ex.getMessage());
        verify(merchantUserRepository, never()).delete(any());
    }

    @Test
    void shouldThrowNotFoundWhenInviteUserMissing() {
        UUID merchantId = UUID.randomUUID();
        UUID invitedBy = UUID.randomUUID();

        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> memberService.inviteMember(merchantId, "missing@example.com", MerchantUser.Role.VIEWER, invitedBy));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void shouldRejectInviteWhenAlreadyMember() {
        UUID merchantId = UUID.randomUUID();
        UUID invitedBy = UUID.randomUUID();

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("user@example.com");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(merchantUserRepository.existsByUserIdAndMerchantId(user.getId(), merchantId)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> memberService.inviteMember(merchantId, "user@example.com", MerchantUser.Role.VIEWER, invitedBy));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("User already a member", ex.getMessage());
    }

    @Test
    void shouldInviteMemberWithPendingStatus() {
        UUID merchantId = UUID.randomUUID();
        UUID invitedBy = UUID.randomUUID();

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("new@example.com");

        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.of(user));
        when(merchantUserRepository.existsByUserIdAndMerchantId(user.getId(), merchantId)).thenReturn(false);
        when(merchantUserRepository.save(any(MerchantUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MerchantUser result = memberService.inviteMember(
                merchantId, "new@example.com", MerchantUser.Role.DEVELOPER, invitedBy);

        assertEquals(user.getId(), result.getUserId());
        assertEquals(merchantId, result.getMerchantId());
        assertEquals(MerchantUser.Role.DEVELOPER, result.getRole());
        assertEquals(invitedBy, result.getInvitedBy());
        assertEquals(MerchantUser.MemberStatus.PENDING_INVITE, result.getStatus());
    }
}
