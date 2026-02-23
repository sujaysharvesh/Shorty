package com.example.Shorty.ratelimit;

import com.example.Shorty.user.CustomUserDetails;
import com.example.Shorty.user.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    private static final long USER_CAPACITY = 100;
    private static final long USER_REFILL_RATE = 60; // tokens per minute
    private static final long ANON_CAPACITY = 20;
    private static final long ANON_REFILL_RATE = 60;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (shouldSkipRateLimit(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        String userId = null;
        boolean isAuthenticated = false;

        try {
            userId = getUserIdIfAuthenticated();
            isAuthenticated = (userId != null && !userId.isEmpty());
        } catch (Exception e) {
            // User not authenticated - this is expected for public endpoints
            log.debug("No authenticated user found: {}", e.getMessage());
        }

        // Create key and determine limits
        String key;
        long capacity;
        long refillRate;

        if (isAuthenticated) {
            key = "user:" + userId;
            capacity = USER_CAPACITY;
            refillRate = USER_REFILL_RATE;
        } else {
            key = "ip:" + clientIp;
            capacity = ANON_CAPACITY;
            refillRate = ANON_REFILL_RATE;
        }

        // Check if allowed
        boolean allowed = rateLimitService.isAllowed(key, capacity, refillRate);

        // Add rate limit headers (these methods need to be added to RateLimitService)
        addRateLimitHeaders(response, key, capacity);

        if (!allowed) {
            log.warn("Rate limit exceeded for {} on path: {}", key, path);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");

            // Calculate retry-after seconds
            long retryAfter = calculateRetryAfter(key, refillRate);

            response.getWriter().write(String.format("""
                {
                    "error": "Too Many Requests",
                    "message": "Rate limit exceeded. Try again later.",
                    "status": 429,
                    "retryAfter": %d
                }
                """, retryAfter));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void addRateLimitHeaders(HttpServletResponse response, String key, long capacity) {
        try {
            long remaining = rateLimitService.getRemainingTokens(key);
            long resetTime = rateLimitService.getResetTimeSeconds(key);

            response.setHeader("X-RateLimit-Limit", String.valueOf(capacity));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, remaining)));
            response.setHeader("X-RateLimit-Reset", String.valueOf(resetTime));
        } catch (Exception e) {
            log.debug("Could not add rate limit headers: {}", e.getMessage());
            // Continue without headers - non-critical
        }
    }


    private long calculateRetryAfter(String key, long refillRate) {
        try {
            long remaining = rateLimitService.getRemainingTokens(key);
            if (remaining <= 0) {
                // If no tokens, estimate time for next token
                return (long) Math.ceil(60.0 / refillRate);
            }
        } catch (Exception e) {
            log.debug("Could not calculate retry-after: {}", e.getMessage());
        }
        return 60; // Default fallback
    }

    public boolean shouldSkipRateLimit(String path) {
        return path.startsWith("/public") ||
                path.startsWith("/health") ||
                path.startsWith("/actuator") ||
                path.startsWith("/favicon") ||
                path.matches(".*\\.(css|js|png|jpg|ico|svg|woff|woff2|ttf|eot)$");
    }

    public String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Get the first IP in the list (client IP)
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    private String getUserIdIfAuthenticated() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication.getPrincipal() == null ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        if (authentication.getPrincipal() instanceof CustomUserDetails user) {
            return user.getUserId();
        }

        return null;
    }
}