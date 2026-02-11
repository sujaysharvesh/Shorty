package com.example.Shorty.Url;


import com.example.Shorty.DTOs.Urls.CreateUrlRequest;
import com.example.Shorty.DTOs.Urls.UrlResponse;
import com.example.Shorty.exception.BadRequestException;
import com.example.Shorty.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepo urlRepo;
    private final ShortCodeGenerator shortCodeGenerator;

    @Value("${app.base-url}")
    private String baseUrl;

    private final UrlValidator urlValidator = new UrlValidator(new String[]{"http", "https"});

    public UrlResponse createUrl(CreateUrlRequest request, String userId) {

        if(!urlValidator.isValid(request.getOriginalUrl())) {
            throw new BadRequestException("Invalid Url format");
        }

        Instant expiresIn = null;
        if(request.getExpiresInDays() != null) {
            expiresIn = Instant.now().plus(request.getExpiresInDays(), ChronoUnit.DAYS);
        }

        String urlId = UUID.randomUUID().toString();
        String shortCode = shortCodeGenerator.generateShortCode(urlId, request.getOriginalUrl());


        Url url = Url.builder()
                .id(urlId)
                .originalUrl(request.getOriginalUrl())
                .shortCode(shortCode)
                .clickCount(0)
                .userId(userId)
                .createdAt(Instant.now())
                .expiresAt(expiresIn)
                .active(true)
                .build();

        Url savedUrl = urlRepo.saveUrl(url);

        return mapToUrlresponse(savedUrl);
    }

    public String getOriginalUrl(String shortCode) {

        Url url = urlRepo.findByShortCode(shortCode).orElseThrow(
                () -> new ResourceNotFoundException("Url Not Found")
        );

        if(url.getExpiresAt() != null && url.getExpiresAt().isBefore(Instant.now())) {
            throw new ResourceNotFoundException("Url Not Found");
        }

        if (!url.isActive()) {
            throw new ResourceNotFoundException("Short Url is Not Active");
        }

        incrementClickCount(url);

        return url.getOriginalUrl();
    }

    private void incrementClickCount(Url url) {
        url.setClickCount(url.getClickCount() + 1);
        urlRepo.saveUrl(url);

    }

    public UrlResponse mapToUrlresponse(Url url) {
        return UrlResponse.builder()
                .shortUrl(baseUrl + "/" + url.getShortCode())
                .originalUrl(url.getOriginalUrl())
                .clickCount(url.getClickCount())
                .expiresAt(url.getExpiresAt())
                .active(url.isActive())
                .createAt(url.getCreatedAt())
                .updateAt(url.getUpdatedAt())
                .build();
    }
}
