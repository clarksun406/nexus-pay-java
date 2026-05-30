package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.domain.entity.PaymentIntent;
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
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private com.nexuspay.common.util.JwtUtil jwtUtil;
    @Mock private com.nexuspay.common.util.CryptoUtil cryptoUtil;
    
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
        
        when(jwtUtil.generateAccessToken(any(), any())).thenReturn("access");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("refresh");
        
        var result = authService.register(new AuthService.RegisterRequest(
            "test@example.com", "password", "Org", "Merchant"));
        
        assertNotNull(result.accessToken());
        assertEquals("access", result.accessToken());
    }
    
    @Test
    void shouldRejectDuplicateEmail() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        
        assertThrows(BusinessException.class, () -> 
            authService.register(new AuthService.RegisterRequest(
                "test@example.com", "password", "Org", "Merchant")));
    }
}
