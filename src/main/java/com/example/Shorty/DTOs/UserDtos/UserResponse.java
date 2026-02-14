package com.example.Shorty.DTOs.UserDtos;

import com.example.Shorty.user.Role;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
//    private String userId;
    private String email;
    private String username;
//    private String apiKey;
    private boolean active;
    private Role role;
    private Instant createdAt;
    private Instant updatedAt;
}