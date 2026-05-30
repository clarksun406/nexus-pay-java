package com.nexuspay.common.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;

class BusinessExceptionTest {
    
    @Test
    void shouldCreateWithMessage() {
        var ex = new BusinessException("Test error");
        assertEquals("Test error", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }
    
    @Test
    void shouldCreateWithMessageAndStatus() {
        var ex = new BusinessException("Not found", HttpStatus.NOT_FOUND);
        assertEquals("Not found", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }
}
