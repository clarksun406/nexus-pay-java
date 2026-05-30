package com.nexuspay.common.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    
    private final Map<String, RateLimitEntry> authLimits = new ConcurrentHashMap<>();
    private final Map<String, RateLimitEntry> pubLimits = new ConcurrentHashMap<>();
    private final Map<String, RateLimitEntry> apiLimits = new ConcurrentHashMap<>();
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        String clientIp = getClientIp(request);
        
        if (path.startsWith("/api/v1/auth")) {
            if (!checkLimit(authLimits, clientIp, 20, 60)) {
                response.setStatus(429);
                response.getWriter().write("{\"error\":\"Rate limit exceeded\"}");
                return;
            }
        } else if (path.startsWith("/pub")) {
            if (!checkLimit(pubLimits, clientIp, 60, 60)) {
                response.setStatus(429);
                response.getWriter().write("{\"error\":\"Rate limit exceeded\"}");
                return;
            }
        } else if (path.startsWith("/api/v1/payment-intents")) {
            String apiKey = request.getHeader("Authorization");
            String key = apiKey != null ? apiKey : clientIp;
            if (!checkLimit(apiLimits, key, 120, 60)) {
                response.setStatus(429);
                response.getWriter().write("{\"error\":\"Rate limit exceeded\"}");
                return;
            }
        }
        
        chain.doFilter(request, response);
    }
    
    private boolean checkLimit(Map<String, RateLimitEntry> limits, String key, int maxRequests, int windowSeconds) {
        long now = Instant.now().getEpochSecond();
        long windowStart = now - (now % windowSeconds);
        
        RateLimitEntry entry = limits.computeIfAbsent(key, k -> new RateLimitEntry());
        
        if (entry.windowStart != windowStart) {
            entry.windowStart = windowStart;
            entry.count.set(0);
        }
        
        return entry.count.incrementAndGet() <= maxRequests;
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        return ip != null ? ip.split(",")[0].trim() : request.getRemoteAddr();
    }
    
    private static class RateLimitEntry {
        long windowStart;
        AtomicInteger count = new AtomicInteger(0);
    }
}
