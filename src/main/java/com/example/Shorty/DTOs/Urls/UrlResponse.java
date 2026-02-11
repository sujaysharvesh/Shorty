package com.example.Shorty.DTOs.Urls;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.Instant;


@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UrlResponse {

    private String originalUrl;
    private String shortUrl;
    private int clickCount;
    private boolean active;
    private Instant expiresAt;
    private Instant createAt;
    private Instant updateAt;


}
