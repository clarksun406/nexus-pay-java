package com.nexuspay.web.security;

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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final Pattern MERCHANT_PATH =
            Pattern.compile("^/api/v1/merchants/([0-9a-fA-F\\-]{36})(?:/.*)?$");

    private final ApiKeyRepository apiKeyRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String apiKey = extractApiKey(request);
        if (apiKey == null) {
            chain.doFilter(request, response);
            return;
        }

        Optional<ApiKey> stored = apiKeyRepository.findByKeyHash(hashKey(apiKey));
        if (stored.isEmpty() || stored.get().getStatus() != ApiKey.KeyStatus.ACTIVE) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Invalid API key\"}");
            return;
        }

        ApiKey key = stored.get();
        UUID pathMerchantId = extractMerchantId(request.getRequestURI());
        if (pathMerchantId != null && !pathMerchantId.equals(key.getMerchantId())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\":\"API key cannot access this merchant\"}");
            return;
        }

        key.setLastUsedAt(Instant.now());
        apiKeyRepository.save(key);

        request.setAttribute("merchantId", key.getMerchantId());
        request.setAttribute("apiKeyId", key.getId());

        var auth = new UsernamePasswordAuthenticationToken(
                key.getMerchantId(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_MERCHANT"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        chain.doFilter(request, response);
    }

    private String extractApiKey(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("sk_")) {
            return header;
        }
        if (header != null && header.startsWith("Bearer sk_")) {
            return header.substring("Bearer ".length());
        }
        String xApiKey = request.getHeader("X-API-Key");
        return xApiKey != null && xApiKey.startsWith("sk_") ? xApiKey : null;
    }

    private UUID extractMerchantId(String uri) {
        var matcher = MERCHANT_PATH.matcher(uri);
        return matcher.matches() ? UUID.fromString(matcher.group(1)) : null;
    }

    private String hashKey(String key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(key.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash API key", e);
        }
    }
}
