package com.nexuspay.web.controller;

import com.nexuspay.common.util.JwtUtil;
import com.nexuspay.repository.ApiKeyRepository;
import com.nexuspay.repository.MerchantUserRepository;
import org.springframework.boot.test.mock.mockito.MockBean;

abstract class WebMvcSecurityTestSupport {

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private ApiKeyRepository apiKeyRepository;

    @MockBean
    private MerchantUserRepository merchantUserRepository;
}
