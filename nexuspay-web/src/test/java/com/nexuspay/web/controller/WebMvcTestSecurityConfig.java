package com.nexuspay.web.controller;

import com.nexuspay.common.exception.GlobalExceptionHandler;
import com.nexuspay.web.config.WebSecurityConfig;
import com.nexuspay.web.security.AdminJwtFilter;
import com.nexuspay.web.security.ApiKeyAuthenticationFilter;
import com.nexuspay.web.security.JwtAuthenticationFilter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({
        WebSecurityConfig.class,
        GlobalExceptionHandler.class,
        AdminJwtFilter.class,
        ApiKeyAuthenticationFilter.class,
        JwtAuthenticationFilter.class
})
class WebMvcTestSecurityConfig {
}
