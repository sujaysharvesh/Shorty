package com.example.Shorty.Url;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisCacheService {

    private final UrlRepo urlRepo;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String URL_CACHE_PREFIX = "url:";
    private static final String URL_ID_PREFIX = "url:id:";
    private static final String RATE_LIMIT_PREFIX = "ratelimit:";
    private static final String CLICK_COUNTER_PREFIX = "url:clicks:";
    private static final Duration DEFAULT_TTL = Duration.ofHours(1);

    public void cacheUrl(String shortCode, String originalUrl, String urlId) {
        try {
            String key = URL_CACHE_PREFIX + shortCode;
            redisTemplate.opsForValue().set(key, originalUrl, DEFAULT_TTL);
            redisTemplate.opsForValue().set(URL_CACHE_PREFIX + shortCode, urlId, DEFAULT_TTL);
        } catch (Exception e) {
            log.error("Failed to cache URL: {}", shortCode, e);
        }
    }

    public String getCachedUrl(String shortCode) {
        try {
            return redisTemplate.opsForValue()
                    .get(URL_CACHE_PREFIX + shortCode);
        } catch (Exception e) {
            log.error("Failed to get cached URL for shortCode: {}", shortCode, e);
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

    public void increaseCount(String shortCode) {
        try {
            redisTemplate.opsForValue().increment(CLICK_COUNTER_PREFIX + shortCode);
        } catch (Exception e) {
            log.error("Failed to increment click count for shortCode: {}", shortCode, e);
        }
    }

    @Scheduled(fixedRate = 60000) // every 1 minute
    public void syncClicksToDb() {

        ScanOptions options = ScanOptions.scanOptions()
                .match(CLICK_COUNTER_PREFIX + "*")
                .count(100)
                .build();

        Cursor<byte[]> cursor = redisTemplate.executeWithStickyConnection(
                connection -> connection.scan(options)
        );

        if (cursor == null) return;

        List<String> keys = new ArrayList<>();
        while (cursor.hasNext()) {
            keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
        }

        if (keys.isEmpty()) return;

        List<Object> ids = redisTemplate.executePipelined(
                (RedisCallback<Object>) connection -> {
                    for (String key : keys) {
                        String shortCode = key.replace(CLICK_COUNTER_PREFIX, "");
                        connection.stringCommands()
                                .get(("url:id:" + shortCode).getBytes(StandardCharsets.UTF_8));
                    }
                    return null;
                }
        );

        List<Object> counts = redisTemplate.executePipelined(
                (RedisCallback<Object>) connection -> {
                    for (String key : keys) {
                        connection.stringCommands()
                                .get(key.getBytes(StandardCharsets.UTF_8));
                    }
                    return null;
                }
        );

        for (int i = 0; i < keys.size(); i++) {
            String urlId = (String) ids.get(i);
            String value = (String) counts.get(i);
            long clickCount = value == null ? 0 : Long.parseLong(value);

            if (clickCount > 0 && urlId != null) {
                urlRepo.incrementClickCount(urlId, clickCount);
            }
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