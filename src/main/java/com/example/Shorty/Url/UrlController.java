package com.example.Shorty.Url;


import com.example.Shorty.DTOs.Urls.CreateUrlRequest;
import com.example.Shorty.DTOs.Urls.UrlResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/url")
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @PostMapping("/create")
    public ResponseEntity<UrlResponse> createShortUrl(
            @Valid @RequestBody CreateUrlRequest createUrlRequest) {

        String userId;
        if (getUserIdFromSecurityContext() == null) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(UrlResponse.builder().build());
        }
        UrlResponse url = urlService.createUrl(createUrlRequest, getUserIdFromSecurityContext());
        return ResponseEntity.status(HttpStatus.CREATED).body(url);

    }

    public String getUserIdFromSecurityContext() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        String userId = (String) authentication.getPrincipal();
        return userId;

    }

}
