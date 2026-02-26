package com.example.Shorty.Url.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateUrlRequest {

    @NotBlank(message = "Original URL is required")
//    @Pattern(regexp = "^https?://.*", message = "URL must start with http:// or https://")
    private String originalUrl;

    private int expiresInMins;

}
