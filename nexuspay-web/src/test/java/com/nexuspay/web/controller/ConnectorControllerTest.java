package com.nexuspay.web.controller;

import com.nexuspay.service.ConnectorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConnectorController.class)
@Import(WebMvcTestSecurityConfig.class)
class ConnectorControllerTest extends WebMvcSecurityTestSupport {
    
    @Autowired private MockMvc mockMvc;

    @MockBean
    private ConnectorService connectorService;
    
    @Test
    void shouldRequireAuthForConnectors() throws Exception {
        mockMvc.perform(get("/api/v1/connectors"))
            .andExpect(status().isForbidden());
    }
}
