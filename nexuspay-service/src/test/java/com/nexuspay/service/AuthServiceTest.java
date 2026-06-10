package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.domain.entity.Role;
import com.nexuspay.repository.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    
    @Mock private UserRepository userRepository;
    @Mock private OrganizationRepository organizationRepository;
    @Mock private MerchantRepository merchantRepository;
    @Mock private MerchantUserRepository merchantUserRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private UserRoleRepository userRoleRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private com.nexuspay.common.util.JwtUtil jwtUtil;
    @Mock private com.nexuspay.common.util.CryptoUtil cryptoUtil;
    @Mock private EmailService emailService;
    
    @InjectMocks private AuthService authService;
    
    @BeforeEach
    void setup() { MockitoAnnotations.openMocks(this); }
    
    @Test
    void shouldRegisterUser() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("hashed");
        
        var user = new com.nexuspay.domain.entity.User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        when(userRepository.save(any())).thenReturn(user);
        
        var org = new com.nexuspay.domain.entity.Organization();
        org.setId(UUID.randomUUID());
        when(organizationRepository.save(any())).thenReturn(org);
        
        var merchant = new com.nexuspay.domain.entity.Merchant();
        merchant.setId(UUID.randomUUID());
        when(merchantRepository.save(any())).thenReturn(merchant);

        var ownerRole = new Role();
        ownerRole.setId(UUID.randomUUID());
        ownerRole.setCode("MERCHANT_OWNER");
        when(roleRepository.findByCode("MERCHANT_OWNER")).thenReturn(Optional.of(ownerRole));
        when(userRoleRepository.existsByUserIdAndRoleIdAndScopeTypeAndScopeId(
                user.getId(), ownerRole.getId(), "MERCHANT", merchant.getId())).thenReturn(false);
        
        when(jwtUtil.generateAccessToken(any(), any())).thenReturn("access");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("refresh");
        
        var result = authService.register(new AuthService.RegisterRequest(
            "test@example.com", "password", "Org", "Merchant"));
        
        assertNotNull(result.accessToken());
        assertEquals("access", result.accessToken());
        verify(userRoleRepository).save(any());
    }
    
    @Test
    void shouldRejectDuplicateEmail() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        
        assertThrows(BusinessException.class, () -> 
            authService.register(new AuthService.RegisterRequest(
                "test@example.com", "password", "Org", "Merchant")));
    }

    @Test
    void shouldRejectAdminRefreshTokenOnMerchantRefresh() {
        when(jwtUtil.validateToken("admin-refresh")).thenReturn(true);
        when(jwtUtil.isRefreshToken("admin-refresh")).thenReturn(true);
        when(jwtUtil.isAdminToken("admin-refresh")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                authService.refresh("admin-refresh"));

        assertEquals(org.springframework.http.HttpStatus.UNAUTHORIZED, ex.getStatus());
        verify(refreshTokenRepository, never()).findByTokenHash(anyString());
    }
}
