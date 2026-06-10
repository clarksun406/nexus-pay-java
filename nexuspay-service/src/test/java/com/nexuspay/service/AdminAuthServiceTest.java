package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.common.util.CryptoUtil;
import com.nexuspay.common.util.JwtUtil;
import com.nexuspay.repository.RefreshTokenRepository;
import com.nexuspay.repository.UserRepository;
import com.nexuspay.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminAuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserRoleRepository userRoleRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private CryptoUtil cryptoUtil;

    @InjectMocks private AdminAuthService adminAuthService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldRejectMerchantRefreshTokenOnAdminRefresh() {
        when(jwtUtil.validateToken("merchant-refresh")).thenReturn(true);
        when(jwtUtil.isRefreshToken("merchant-refresh")).thenReturn(true);
        when(jwtUtil.isAdminToken("merchant-refresh")).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                adminAuthService.refresh("merchant-refresh"));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        verify(refreshTokenRepository, never()).findByTokenHash(anyString());
    }
}
