package com.nexuspay.web.controller;

import com.nexuspay.service.ApiKeyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApiKeyController.class)
@Import(WebMvcTestSecurityConfig.class)
class ApiKeyControllerTest extends WebMvcSecurityTestSupport {
    
    @Autowired private MockMvc mockMvc;

    @MockBean
    private ApiKeyService apiKeyService;
    
    @Test
    void shouldRequireAuthForApiKeys() throws Exception {
        mockMvc.perform(get("/api/v1/api-keys"))
            .andExpect(status().isForbidden());
    }
}
