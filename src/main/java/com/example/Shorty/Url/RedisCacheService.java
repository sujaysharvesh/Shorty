package com.example.Shorty.Url;


import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String URL_CACHE_PREFIX = "url:";
    private static final String RATE_LIMIT_PREFIX = "ratelimit:";
    private static final String COUNTER_KEY = "counter:shortcode";
    private static final Duration DEFAULT_TTL = Duration.ofHours(1);

    public void cacheUrl(String shortCode, String originalUrl) {
        try {

            String key = URL_CACHE_PREFIX + shortCode;
            redisTemplate.opsForValue().set(key, originalUrl, DEFAULT_TTL);

        } catch (Exception e) {
            log.error("Failed to cache URL: {}", shortCode, e);
        }
    }

    public String getCachedUrl(String shortCode) {
        try {

            String key = URL_CACHE_PREFIX + shortCode;
            String url = redisTemplate.opsForValue().get(key);

            return url;
        }catch (Exception e) {
            log.error("Failed to get cached URL: {}", shortCode, e);
            return null;
        }
    }

    public void invalidateCache(String shortCode) {
        try {
            String key = URL_CACHE_PREFIX + shortCode;
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Failed to invalidate cache: {}", shortCode, e);
        }
    }

    public boolean isRateLimited(String ipAddress, int maxRequests, int windowSeconds) {
        try {
            String key = RATE_LIMIT_PREFIX + ipAddress;
            Long currentCount = redisTemplate.opsForValue().increment(key);

            if (currentCount == 1) {
                redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
            }

            boolean limited = currentCount > maxRequests;
            if (limited) {
                log.warn("Rate limit exceeded for IP: {}", ipAddress);
            }
            return limited;
        } catch (Exception e) {
            log.error("Rate limit check failed for IP: {}", ipAddress, e);
            return false;
        }
    }

    public Long getNextCounter() {
        try {
            return redisTemplate.opsForValue().increment(COUNTER_KEY);
        } catch (Exception e) {
            log.error("Failed to generate counter", e);
            return System.currentTimeMillis();
        }
    }

    public boolean isRedisAvailable() {
        try {
            redisTemplate.opsForValue().get("health-check");
            return true;
        } catch (Exception e) {
            log.error("Redis is not available", e);
            return false;
        }
    }

}
