package com.nexuspay.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentIntentControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldRequireAuthForPaymentIntents() throws Exception {
        mockMvc.perform(get("/api/v1/payment-intents"))
            .andExpect(status().isForbidden());
    }
}
