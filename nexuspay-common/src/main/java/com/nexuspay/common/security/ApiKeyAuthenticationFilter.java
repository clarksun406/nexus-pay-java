package com.nexuspay.common.security;

import com.nexuspay.domain.entity.ApiKey;
import com.nexuspay.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {
    
    private final ApiKeyRepository apiKeyRepository;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("sk_")) {
            chain.doFilter(request, response);
            return;
        }
        
        String apiKey = authHeader;
        String keyHash = hashKey(apiKey);
        
        apiKeyRepository.findByKeyHash(keyHash).ifPresent(key -> {
            if (key.getStatus() == ApiKey.KeyStatus.ACTIVE) {
                var auth = new UsernamePasswordAuthenticationToken(
                        key.getMerchantId(), null, 
                        Collections.singletonList(new SimpleGrantedAuthority("MERCHANT")));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        });
        
        chain.doFilter(request, response);
    }
    
    private String hashKey(String key) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(key.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
