package com.example.Shorty.Url;


import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Random;

@Component
public class ShortCodeGenerator {

    private static final String BASE64_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Integer SHORT_LENGTH = 7;
    private static final Random random = new SecureRandom();

    public String generateShortCode(String urlId, String originalUrl) {

        String input = urlId + originalUrl + System.nanoTime();

        int hash = Math.abs(input.hashCode());

        StringBuilder shortCode = new StringBuilder();

        while (shortCode.length() < SHORT_LENGTH) {
            shortCode.append(BASE64_CHARS.charAt(hash % BASE64_CHARS.length()));
            hash = hash / BASE64_CHARS.length();

            if (hash == 0) {
                hash = random.nextInt();
            }
        }

        return shortCode.toString();
    }


}
