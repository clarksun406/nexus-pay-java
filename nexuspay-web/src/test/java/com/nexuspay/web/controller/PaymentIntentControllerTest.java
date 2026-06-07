package com.nexuspay.web.controller;

import com.nexuspay.service.PaymentIntentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentIntentController.class)
@Import(WebMvcTestSecurityConfig.class)
class PaymentIntentControllerTest extends WebMvcSecurityTestSupport {
    
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentIntentService paymentIntentService;
    
    @Test
    void shouldRequireAuthForPaymentIntents() throws Exception {
        mockMvc.perform(get("/api/v1/payment-intents"))
            .andExpect(status().isForbidden());
    }
}
