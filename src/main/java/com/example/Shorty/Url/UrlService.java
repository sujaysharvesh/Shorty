package com.example.Shorty.Url;


import com.example.Shorty.DTOs.Urls.CreateUrlRequest;
import com.example.Shorty.DTOs.Urls.UrlResponse;
import com.example.Shorty.exception.BadRequestException;
import com.example.Shorty.exception.ResourceNotFoundException;
import com.example.Shorty.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepo urlRepo;
    private final ShortCodeGenerator shortCodeGenerator;
    private final UserService userService;

    @Value("${app.base-url}")
    private String baseUrl;

    private final UrlValidator urlValidator = new UrlValidator(new String[]{"http", "https"});

    public UrlResponse createUrl(CreateUrlRequest request) {

        String userId = userService.getUserIdFromSecurityContext();

        if(!urlValidator.isValid(request.getOriginalUrl())) {
            throw new BadRequestException("Invalid Url format");
        }

        Instant expiresIn = Instant.now().plus(request.getExpiresInDays(), ChronoUnit.DAYS);

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

        return mapToUrlResponse(savedUrl);
    }

    public String getOriginalUrl(String shortCode) {
        log.info(shortCode);
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

    public List<UrlResponse> getAllUserUrls() {
        String userId = userService.getUserIdFromSecurityContext();

        List<Url> urls = urlRepo.findUserUrls(userId);
        return urls.stream().map(this::mapToUrlResponse).toList();

    }

    public String deleteUrl(String shortCode) {
        String userId = userService.getUserIdFromSecurityContext();

        urlRepo.deleteByShortCodeAndUserId(shortCode, userId);
        return "Done";
    }

    private void incrementClickCount(Url url) {
        url.setClickCount(url.getClickCount() + 1);
        urlRepo.saveUrl(url);

    }

    public UrlResponse mapToUrlResponse(Url url) {
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
