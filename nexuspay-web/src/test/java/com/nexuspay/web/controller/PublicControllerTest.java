package com.nexuspay.web.controller;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.service.PaymentLinkService;
import com.nexuspay.service.ThreeDsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PublicController.class)
@Import(WebMvcTestSecurityConfig.class)
class PublicControllerTest extends WebMvcSecurityTestSupport {
    
    @Autowired private MockMvc mockMvc;

    @MockBean
    private PaymentLinkService paymentLinkService;

    @MockBean
    private ThreeDsService threeDsService;
    
    @Test
    void shouldReturnNotFoundForInvalidToken() throws Exception {
        when(paymentLinkService.getByToken("invalid-token"))
                .thenThrow(new BusinessException("Payment link not found", HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/pub/pay/invalid-token"))
            .andExpect(status().isNotFound());
    }
}
