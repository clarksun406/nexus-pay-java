package com.nexuspay.web.security;

import com.nexuspay.common.util.JwtUtil;
import com.nexuspay.domain.entity.ApiKey;
import com.nexuspay.domain.entity.MerchantUser;
import com.nexuspay.repository.ApiKeyRepository;
import com.nexuspay.repository.MerchantUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for merchant tenant enforcement in JWT and API-key filters.
 * Tests filter behavior directly without MockMvc, allowing precise control
 * over hash computation and URL routing.
 */
class MerchantTenantSecurityTest {

    private final UUID merchantA = UUID.randomUUID();
    private final UUID merchantB = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    // ---- JwtAuthenticationFilter tests ----

    @Test
    void jwtShouldRejectCrossMerchantPathAccess() throws Exception {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        MerchantUserRepository merchantUserRepo = mock(MerchantUserRepository.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil, merchantUserRepo);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        String token = "valid_jwt_token";
        MerchantUser membership = activeMembership(userId, merchantA, MerchantUser.Role.ADMIN);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(request.getRequestURI()).thenReturn("/api/v1/merchants/" + merchantB + "/payment-intents");
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.isAccessToken(token)).thenReturn(true);
        when(jwtUtil.getUserId(token)).thenReturn(userId);
        when(merchantUserRepo.findByUserId(userId)).thenReturn(List.of(membership));
        when(response.getWriter()).thenReturn(new java.io.PrintWriter(java.io.OutputStream.nullOutputStream()));

        filter.doFilterInternal(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void jwtShouldAllowOwnMerchantPathAccess() throws Exception {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        MerchantUserRepository merchantUserRepo = mock(MerchantUserRepository.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil, merchantUserRepo);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        String token = "valid_jwt_token";
        MerchantUser membership = activeMembership(userId, merchantA, MerchantUser.Role.ADMIN);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(request.getRequestURI()).thenReturn("/api/v1/merchants/" + merchantA + "/payment-intents");
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.isAccessToken(token)).thenReturn(true);
        when(jwtUtil.getUserId(token)).thenReturn(userId);
        when(merchantUserRepo.findByUserId(userId)).thenReturn(List.of(membership));

        filter.doFilterInternal(request, response, chain);

        verify(response, never()).setStatus(anyInt());
        verify(chain).doFilter(request, response);
        verify(request).setAttribute("merchantId", merchantA);
    }

    @Test
    void jwtShouldRejectWhenUserHasNoActiveMembership() throws Exception {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        MerchantUserRepository merchantUserRepo = mock(MerchantUserRepository.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil, merchantUserRepo);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        String token = "valid_jwt_token";
        MerchantUser inactive = new MerchantUser();
        inactive.setUserId(userId);
        inactive.setMerchantId(merchantA);
        inactive.setRole(MerchantUser.Role.VIEWER);
        inactive.setStatus(MerchantUser.MemberStatus.INACTIVE);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(request.getRequestURI()).thenReturn("/api/v1/merchants/" + merchantA + "/payment-intents");
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.isAccessToken(token)).thenReturn(true);
        when(jwtUtil.getUserId(token)).thenReturn(userId);
        when(merchantUserRepo.findByUserId(userId)).thenReturn(List.of(inactive));
        when(response.getWriter()).thenReturn(new java.io.PrintWriter(java.io.OutputStream.nullOutputStream()));

        filter.doFilterInternal(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void jwtShouldRejectWhenUserHasNoMemberships() throws Exception {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        MerchantUserRepository merchantUserRepo = mock(MerchantUserRepository.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil, merchantUserRepo);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        String token = "valid_jwt_token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(request.getRequestURI()).thenReturn("/api/v1/merchants/" + merchantA + "/payment-intents");
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.isAccessToken(token)).thenReturn(true);
        when(jwtUtil.getUserId(token)).thenReturn(userId);
        when(merchantUserRepo.findByUserId(userId)).thenReturn(List.of());
        when(response.getWriter()).thenReturn(new java.io.PrintWriter(java.io.OutputStream.nullOutputStream()));

        filter.doFilterInternal(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void jwtShouldRejectRefreshToken() throws Exception {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        MerchantUserRepository merchantUserRepo = mock(MerchantUserRepository.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil, merchantUserRepo);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        String refreshToken = "refresh_token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + refreshToken);
        when(request.getRequestURI()).thenReturn("/api/v1/merchants/" + merchantA + "/payment-intents");
        when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
        when(jwtUtil.isAccessToken(refreshToken)).thenReturn(false);
        when(response.getWriter()).thenReturn(new java.io.PrintWriter(java.io.OutputStream.nullOutputStream()));

        filter.doFilterInternal(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void jwtShouldRejectInvalidToken() throws Exception {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        MerchantUserRepository merchantUserRepo = mock(MerchantUserRepository.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil, merchantUserRepo);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        String invalidToken = "invalid_token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(request.getRequestURI()).thenReturn("/api/v1/payment-intents");
        when(jwtUtil.validateToken(invalidToken)).thenReturn(false);
        when(response.getWriter()).thenReturn(new java.io.PrintWriter(java.io.OutputStream.nullOutputStream()));

        filter.doFilterInternal(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void jwtShouldSkipNonBearerHeader() throws Exception {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        MerchantUserRepository merchantUserRepo = mock(MerchantUserRepository.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil, merchantUserRepo);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");
        when(request.getRequestURI()).thenReturn("/api/v1/payment-intents");

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(jwtUtil, never()).validateToken(anyString());
    }

    @Test
    void jwtShouldSkipApiKeyBearerHeader() throws Exception {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        MerchantUserRepository merchantUserRepo = mock(MerchantUserRepository.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil, merchantUserRepo);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getHeader("Authorization")).thenReturn("Bearer sk_test_key");
        when(request.getRequestURI()).thenReturn("/api/v1/payment-intents");

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(jwtUtil, never()).validateToken(anyString());
    }

    @Test
    void jwtShouldPassForNonMerchantScopedEndpoint() throws Exception {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        MerchantUserRepository merchantUserRepo = mock(MerchantUserRepository.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil, merchantUserRepo);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        String token = "valid_jwt_token";
        MerchantUser membership = activeMembership(userId, merchantA, MerchantUser.Role.ADMIN);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(request.getRequestURI()).thenReturn("/api/v1/payment-intents");
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.isAccessToken(token)).thenReturn(true);
        when(jwtUtil.getUserId(token)).thenReturn(userId);
        when(merchantUserRepo.findByUserId(userId)).thenReturn(List.of(membership));

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(request).setAttribute("userId", userId);
    }

    @Test
    void jwtShouldSkipAdminPaths() throws Exception {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        MerchantUserRepository merchantUserRepo = mock(MerchantUserRepository.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil, merchantUserRepo);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getRequestURI()).thenReturn("/api/v1/admin/overview");
        when(request.getHeader("Authorization")).thenReturn("Bearer some_token");

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(jwtUtil, never()).validateToken(anyString());
    }

    // ---- ApiKeyAuthenticationFilter tests ----

    @Test
    void apiKeyShouldRejectCrossMerchantPathAccess() throws Exception {
        ApiKeyRepository apiKeyRepo = mock(ApiKeyRepository.class);
        ApiKeyAuthenticationFilter filter = new ApiKeyAuthenticationFilter(apiKeyRepo);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        ApiKey key = activeApiKey(merchantA);

        when(request.getHeader("Authorization")).thenReturn("sk_test_key");
        when(request.getRequestURI()).thenReturn("/api/v1/merchants/" + merchantB + "/payment-intents");
        when(apiKeyRepo.findByKeyHash(anyString())).thenReturn(Optional.of(key));
        when(response.getWriter()).thenReturn(new java.io.PrintWriter(java.io.OutputStream.nullOutputStream()));

        filter.doFilterInternal(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void apiKeyShouldAllowOwnMerchantPathAccess() throws Exception {
        ApiKeyRepository apiKeyRepo = mock(ApiKeyRepository.class);
        ApiKeyAuthenticationFilter filter = new ApiKeyAuthenticationFilter(apiKeyRepo);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        ApiKey key = activeApiKey(merchantA);

        when(request.getHeader("Authorization")).thenReturn("sk_test_key");
        when(request.getRequestURI()).thenReturn("/api/v1/merchants/" + merchantA + "/payment-intents");
        when(apiKeyRepo.findByKeyHash(anyString())).thenReturn(Optional.of(key));

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(request).setAttribute("merchantId", merchantA);
    }

    @Test
    void apiKeyShouldRejectRevokedKey() throws Exception {
        ApiKeyRepository apiKeyRepo = mock(ApiKeyRepository.class);
        ApiKeyAuthenticationFilter filter = new ApiKeyAuthenticationFilter(apiKeyRepo);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        ApiKey key = new ApiKey();
        key.setId(UUID.randomUUID());
        key.setMerchantId(merchantA);
        key.setStatus(ApiKey.KeyStatus.REVOKED);

        when(request.getHeader("Authorization")).thenReturn("sk_revoked_key");
        when(apiKeyRepo.findByKeyHash(anyString())).thenReturn(Optional.of(key));
        when(response.getWriter()).thenReturn(new java.io.PrintWriter(java.io.OutputStream.nullOutputStream()));

        filter.doFilterInternal(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void apiKeyShouldRejectUnknownKey() throws Exception {
        ApiKeyRepository apiKeyRepo = mock(ApiKeyRepository.class);
        ApiKeyAuthenticationFilter filter = new ApiKeyAuthenticationFilter(apiKeyRepo);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getHeader("Authorization")).thenReturn("sk_unknown_key");
        when(apiKeyRepo.findByKeyHash(anyString())).thenReturn(Optional.empty());
        when(response.getWriter()).thenReturn(new java.io.PrintWriter(java.io.OutputStream.nullOutputStream()));

        filter.doFilterInternal(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void apiKeyShouldAcceptBearerSkPrefix() throws Exception {
        ApiKeyRepository apiKeyRepo = mock(ApiKeyRepository.class);
        ApiKeyAuthenticationFilter filter = new ApiKeyAuthenticationFilter(apiKeyRepo);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        ApiKey key = activeApiKey(merchantA);

        when(request.getHeader("Authorization")).thenReturn("Bearer sk_test_key");
        when(request.getRequestURI()).thenReturn("/api/v1/merchants/" + merchantA + "/payment-intents");
        when(apiKeyRepo.findByKeyHash(anyString())).thenReturn(Optional.of(key));

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(request).setAttribute("merchantId", merchantA);
    }

    @Test
    void apiKeyShouldAcceptXApiKeyHeader() throws Exception {
        ApiKeyRepository apiKeyRepo = mock(ApiKeyRepository.class);
        ApiKeyAuthenticationFilter filter = new ApiKeyAuthenticationFilter(apiKeyRepo);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        ApiKey key = activeApiKey(merchantA);

        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getHeader("X-API-Key")).thenReturn("sk_test_key");
        when(request.getRequestURI()).thenReturn("/api/v1/merchants/" + merchantA + "/payment-intents");
        when(apiKeyRepo.findByKeyHash(anyString())).thenReturn(Optional.of(key));

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(request).setAttribute("merchantId", merchantA);
    }

    @Test
    void apiKeyShouldPassForNonMerchantScopedEndpoint() throws Exception {
        ApiKeyRepository apiKeyRepo = mock(ApiKeyRepository.class);
        ApiKeyAuthenticationFilter filter = new ApiKeyAuthenticationFilter(apiKeyRepo);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        ApiKey key = activeApiKey(merchantA);

        when(request.getHeader("Authorization")).thenReturn("sk_test_key");
        when(request.getRequestURI()).thenReturn("/api/v1/payment-intents");
        when(apiKeyRepo.findByKeyHash(anyString())).thenReturn(Optional.of(key));

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(request).setAttribute("merchantId", merchantA);
    }

    @Test
    void apiKeyShouldUpdateLastUsedTimestamp() throws Exception {
        ApiKeyRepository apiKeyRepo = mock(ApiKeyRepository.class);
        ApiKeyAuthenticationFilter filter = new ApiKeyAuthenticationFilter(apiKeyRepo);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        ApiKey key = activeApiKey(merchantA);

        when(request.getHeader("Authorization")).thenReturn("sk_test_key");
        when(request.getRequestURI()).thenReturn("/api/v1/merchants/" + merchantA + "/payment-intents");
        when(apiKeyRepo.findByKeyHash(anyString())).thenReturn(Optional.of(key));

        filter.doFilterInternal(request, response, chain);

        ArgumentCaptor<ApiKey> captor = ArgumentCaptor.forClass(ApiKey.class);
        verify(apiKeyRepo).save(captor.capture());
        assertNotNull(captor.getValue().getLastUsedAt());
    }

    // ---- helpers ----

    private MerchantUser activeMembership(UUID userId, UUID merchantId, MerchantUser.Role role) {
        MerchantUser member = new MerchantUser();
        member.setUserId(userId);
        member.setMerchantId(merchantId);
        member.setRole(role);
        member.setStatus(MerchantUser.MemberStatus.ACTIVE);
        return member;
    }

    private ApiKey activeApiKey(UUID merchantId) {
        ApiKey key = new ApiKey();
        key.setId(UUID.randomUUID());
        key.setMerchantId(merchantId);
        key.setStatus(ApiKey.KeyStatus.ACTIVE);
        return key;
    }
}