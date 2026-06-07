package com.nexuspay.common.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Redis-backed distributed rate limiter using sliding window counter.
 * Provides token-bucket style rate limiting across multiple instances.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisRateLimiter {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Check if a request is allowed under the rate limit.
     *
     * @param key        unique key for the rate limit (e.g., "ratelimit:auth:user@example.com")
     * @param maxRequests maximum requests allowed in the window
     * @param window     time window duration
     * @return true if request is allowed, false if rate limited
     */
    public boolean tryAcquire(String key, int maxRequests, Duration window) {
        String redisKey = "ratelimit:" + key;
        long now = System.currentTimeMillis();
        long windowStart = now - window.toMillis();

        try {
            var ops = redisTemplate.opsForZSet();

            // Remove expired entries
            ops.removeRangeByScore(redisKey, 0, windowStart);

            // Count current requests in window
            Long count = ops.zCard(redisKey);
            if (count != null && count >= maxRequests) {
                return false;
            }

            // Add current request
            ops.add(redisKey, String.valueOf(now) + "-" + Thread.currentThread().getId(), now);
            redisTemplate.expire(redisKey, window.multipliedBy(2));

            return true;
        } catch (Exception e) {
            log.warn("Redis rate limiter fallback - allowing request: {}", e.getMessage());
            return true; // Fail-open on Redis errors
        }
    }

    /**
     * Simple counter-based rate limiter for basic use cases.
     */
    public boolean tryAcquireCounter(String key, int maxRequests, Duration window) {
        String redisKey = "counter:" + key;
        try {
            Long count = redisTemplate.opsForValue().increment(redisKey);
            if (count != null && count == 1) {
                redisTemplate.expire(redisKey, window);
            }
            return count != null && count <= maxRequests;
        } catch (Exception e) {
            log.warn("Redis counter fallback - allowing request: {}", e.getMessage());
            return true;
        }
    }
}