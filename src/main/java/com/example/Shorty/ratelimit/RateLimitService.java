package com.example.Shorty.ratelimit;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.endpoints.internal.Value;

import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class RateLimitService {


    private final ConcurrentHashMap<String, TokenBucketRateLimiter> buckets = new ConcurrentHashMap<>();

    public boolean isAllowed(String clientIp, long capacity, long refillRate) {

        TokenBucketRateLimiter bucket = buckets.computeIfAbsent(clientIp,
                ip -> new TokenBucketRateLimiter(capacity, refillRate));

        return bucket.tryConsume();
    }

    public boolean isAllowed(String key) {
        return isAllowed(key, 10, 5);
    }

    public void resetBucket(String key) {
        buckets.remove(key);
    }

    public long getResetTimeSeconds(String key) {
        try {
                TokenBucketRateLimiter bucket = buckets.get(key);
                if (bucket != null) {
                    long tokens = bucket.getAvailableTokens();
                    long capacity = bucket.getCapacity();
                    long refillRate = bucket.getRefillRatePerSec();

                    if (tokens < capacity) {
                        return (long) Math.ceil((capacity - tokens) * 60.0 / refillRate);
                    }
                }
                return 0;
        } catch (Exception e) {
            log.debug("Error getting reset time: {}", e.getMessage());
            return 60; // Default fallback
        }
    }

    public long getRemainingTokens(String key) {
        TokenBucketRateLimiter bucket = buckets.get(key);
        return bucket != null ? bucket.getAvailableTokens() : 0;
    }

}
