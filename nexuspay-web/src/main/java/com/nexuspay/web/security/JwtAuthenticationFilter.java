package com.nexuspay.web.security;

import com.nexuspay.common.util.JwtUtil;
import com.nexuspay.domain.entity.MerchantUser;
import com.nexuspay.repository.MerchantUserRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Pattern MERCHANT_PATH =
            Pattern.compile("^/api/v1/merchants/([0-9a-fA-F\\-]{36})(?:/.*)?$");

    private final JwtUtil jwtUtil;
    private final MerchantUserRepository merchantUserRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ") || header.startsWith("Bearer sk_")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        if (!jwtUtil.validateToken(token) || !jwtUtil.isAccessToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Invalid token\"}");
            return;
        }

        UUID userId = jwtUtil.getUserId(token);
        Optional<MerchantUser> membership = resolveMembership(userId, request);
        if (requiresMerchant(request.getRequestURI()) && membership.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\":\"User cannot access this merchant\"}");
            return;
        }

        request.setAttribute("userId", userId);
        membership.ifPresent(member -> {
            request.setAttribute("merchantId", member.getMerchantId());
            request.setAttribute("merchantRole", member.getRole());
        });

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        membership.ifPresent(member -> authorities.add(new SimpleGrantedAuthority("ROLE_" + member.getRole().name())));

        var auth = new UsernamePasswordAuthenticationToken(userId, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        chain.doFilter(request, response);
    }

    private Optional<MerchantUser> resolveMembership(UUID userId, HttpServletRequest request) {
        UUID requestedMerchantId = requestedMerchantId(request);
        List<MerchantUser> memberships = merchantUserRepository.findByUserId(userId).stream()
                .filter(member -> member.getStatus() == MerchantUser.MemberStatus.ACTIVE)
                .toList();

        if (requestedMerchantId != null) {
            return memberships.stream()
                    .filter(member -> requestedMerchantId.equals(member.getMerchantId()))
                    .findFirst();
        }

        return memberships.stream().findFirst();
    }

    private UUID requestedMerchantId(HttpServletRequest request) {
        UUID pathMerchantId = extractMerchantId(request.getRequestURI());
        if (pathMerchantId != null) {
            return pathMerchantId;
        }

        String header = request.getHeader("X-Merchant-Id");
        return header == null || header.isBlank() ? null : UUID.fromString(header);
    }

    private boolean requiresMerchant(String uri) {
        return extractMerchantId(uri) != null;
    }

    private UUID extractMerchantId(String uri) {
        var matcher = MERCHANT_PATH.matcher(uri);
        return matcher.matches() ? UUID.fromString(matcher.group(1)) : null;
    }
}
