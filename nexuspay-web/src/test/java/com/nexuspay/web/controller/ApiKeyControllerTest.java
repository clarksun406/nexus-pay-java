package com.nexuspay.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApiKeyControllerTest {
    
    @Autowired private MockMvc mockMvc;
    
    @Test
    void shouldRequireAuthForApiKeys() throws Exception {
        mockMvc.perform(get("/api/v1/api-keys"))
            .andExpect(status().isForbidden());
    }
}
