package com.example.Shorty.Url;


import com.example.Shorty.common.response.ApiResponse;
import com.example.Shorty.Url.dto.CreateUrlRequest;
import com.example.Shorty.Url.dto.UrlResponse;
import com.example.Shorty.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/url")
@RequiredArgsConstructor
@Slf4j
public class UrlController {

    private final UrlService urlService;
    private final UserService userService;
    private final RedisCacheService cacheService;

    @PostMapping("/shorten")
    public ResponseEntity<ApiResponse<UrlResponse>> createShortUrl(
            @Valid @RequestBody CreateUrlRequest createUrlRequest,
            HttpServletRequest request) {

        String ipAddress = request.getRemoteAddr();
        if (cacheService.isRateLimited(ipAddress, 10, 60)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error("Try again later."));
        }
        UrlResponse url = urlService.createUrl(createUrlRequest);

        return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success(url));

    }


    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<UrlResponse>>> getUserUls() {

        List<UrlResponse> response = urlService.getAllUserUrls();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }


    @DeleteMapping("/{shortcode}")
    public ResponseEntity<Void> deleteUrl(
            @PathVariable String shortcode
    ) {
        urlService.deleteUrl(shortcode);
        return ResponseEntity.noContent().build();
    }


}
