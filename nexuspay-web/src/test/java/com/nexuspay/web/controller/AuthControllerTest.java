package com.nexuspay.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexuspay.service.AuthService;
import com.nexuspay.service.PasswordResetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(WebMvcTestSecurityConfig.class)
class AuthControllerTest extends WebMvcSecurityTestSupport {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private PasswordResetService passwordResetService;
    
    @Test
    void shouldRegisterUser() throws Exception {
        when(authService.register(any(AuthService.RegisterRequest.class)))
                .thenReturn(new AuthService.AuthResponse(
                        "access-token",
                        "refresh-token",
                        UUID.randomUUID(),
                        "test@example.com",
                        UUID.randomUUID(),
                        "Test Merchant"));

        var request = new AuthController.RegisterRequest(
            "test@example.com", "password123", "Test Org", "Test Merchant");
        
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.userId").exists());
    }
}
