package com.example.Shorty.Url;


import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Random;

@Component
public class ShortCodeGenerator {

    private static final String BASE62 =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int SHORT_LENGTH = 7;
    private static final SecureRandom random = new SecureRandom();

    public String generateShortCode(String urlId, String originalUrl) {

        String input = urlId + originalUrl + System.nanoTime();

        int hash = Math.abs(input.hashCode());

        StringBuilder shortCode = new StringBuilder();

        while (shortCode.length() < SHORT_LENGTH) {

            int index = Math.abs(hash % BASE62.length());
            shortCode.append(BASE62.charAt(index));

            hash = hash / BASE62.length();

            if (hash == 0) {
                hash = random.nextInt(Integer.MAX_VALUE);
            }
        }

        return shortCode.toString();
    }
}
