package com.nexuspay.common.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CryptoUtilTest {
    
    private final CryptoUtil cryptoUtil = new CryptoUtil();
    
    @Test
    void shouldGenerateApiKey() {
        String key = cryptoUtil.generateApiKey("sk_test_");
        assertTrue(key.startsWith("sk_test_"));
        assertTrue(key.length() > 10);
    }
    
    @Test
    void shouldGenerateUniqueKeys() {
        String key1 = cryptoUtil.generateApiKey("pk_");
        String key2 = cryptoUtil.generateApiKey("pk_");
        assertNotEquals(key1, key2);
    }
    
    @Test
    void shouldHashSha256() {
        String hash = cryptoUtil.hashSha256("test", "secret");
        assertNotNull(hash);
        assertEquals(44, hash.length()); // Base64 encoded SHA-256
    }
    
    @Test
    void shouldGenerateToken() {
        String token = cryptoUtil.generateToken();
        assertNotNull(token);
        assertTrue(token.length() >= 32);
    }
}
