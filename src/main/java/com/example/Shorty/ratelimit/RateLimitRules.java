package com.example.Shorty.ratelimit;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitRules {

    private Map<String, Rule> rules = new HashMap<>();

    @Data
    public static class Rule {
        private long capacity;
        private long refillRate;
        private String description;
    }

}
