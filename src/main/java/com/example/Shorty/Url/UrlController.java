package com.example.Shorty.Url;


import com.example.Shorty.DTOs.ApiResponse;
import com.example.Shorty.DTOs.Urls.CreateUrlRequest;
import com.example.Shorty.DTOs.Urls.UrlResponse;
import com.example.Shorty.exception.BadRequestException;
import com.example.Shorty.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/url")
@RequiredArgsConstructor
@Slf4j
public class UrlController {

    private final UrlService urlService;
    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<UrlResponse>> createShortUrl(
            @Valid @RequestBody CreateUrlRequest createUrlRequest) {

            String userId = userService.getUserIdFromSecurityContext();

            if (userId == null) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Unauthorized"));
            }

            UrlResponse url = urlService.createUrl(createUrlRequest, userId);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success(url));

    }


    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<UrlResponse>>> getUserUls() {
        String userId = userService.getUserIdFromSecurityContext();

        if (userId == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
        }

        List<UrlResponse> response = urlService.getAllUserUrls(userId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));


    }

}
