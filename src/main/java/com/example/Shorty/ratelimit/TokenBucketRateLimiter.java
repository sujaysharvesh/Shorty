package com.example.Shorty.ratelimit;


import lombok.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


@Getter
public class TokenBucketRateLimiter {

    private final long capacity;
    private final long refillRatePerSec;
    private long tokens;
    private long lastRefillTimeStamp;

    public TokenBucketRateLimiter(long capacity, long refillRatePerSec) {
        this.capacity = capacity;
        this.refillRatePerSec = refillRatePerSec;
        this.tokens = capacity;
        this.lastRefillTimeStamp = System.currentTimeMillis();
    }

    public synchronized boolean tryConsume() {
        refill();
        if (tokens > 0) {
            tokens--;
            return true;
        }
        return false;
    }

    public void refill(){
        long now = System.currentTimeMillis();
        long elapsedMillis = now - lastRefillTimeStamp;
        long tokensToAdd = (elapsedMillis / 1000) * refillRatePerSec;

        if (tokensToAdd > 0) {
            tokens = Math.min(capacity, tokens + tokensToAdd);
            // Only update timestamp when we actually add tokens
            lastRefillTimeStamp = now - (elapsedMillis % 1000); // Keep remainder for accuracy
        }

    }

    public long getAvailableTokens() {
        refill();
        return tokens;
    }


}
