package com.nexuspay.common.util;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {
    
    private final JwtUtil jwtUtil = new JwtUtil();
    private final UUID userId = UUID.randomUUID();
    
    public JwtUtilTest() {
        ReflectionTestUtils.setField(jwtUtil, "secret", "test-secret-must-be-at-least-256-bits-long-for-hmac-sha256");
        ReflectionTestUtils.setField(jwtUtil, "accessTokenExpiration", 900000L);
        ReflectionTestUtils.setField(jwtUtil, "refreshTokenExpiration", 604800000L);
    }
    
    @Test
    void shouldGenerateAccessToken() {
        String token = jwtUtil.generateAccessToken(userId, "test@example.com");
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);
    }
    
    @Test
    void shouldGenerateRefreshToken() {
        String token = jwtUtil.generateRefreshToken(userId);
        assertNotNull(token);
    }
    
    @Test
    void shouldValidateToken() {
        String token = jwtUtil.generateAccessToken(userId, "test@example.com");
        assertTrue(jwtUtil.validateToken(token));
    }
    
    @Test
    void shouldExtractUserId() {
        String token = jwtUtil.generateAccessToken(userId, "test@example.com");
        UUID extracted = jwtUtil.getUserId(token);
        assertEquals(userId, extracted);
    }
    
    @Test
    void shouldRejectInvalidToken() {
        assertFalse(jwtUtil.validateToken("invalid.token.here"));
    }
}
