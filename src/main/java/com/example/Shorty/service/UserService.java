package com.example.Shorty.service;


import com.example.Shorty.DTOs.UserDtos.AuthResponse;
import com.example.Shorty.DTOs.UserDtos.LoginRequest;
import com.example.Shorty.DTOs.UserDtos.RegisterRequest;
import com.example.Shorty.DTOs.UserDtos.UserResponse;
import com.example.Shorty.Utils.JwtUtils;
import com.example.Shorty.exception.BadRequestException;
import com.example.Shorty.exception.ResourceNotFoundException;
import com.example.Shorty.user.Role;
import com.example.Shorty.user.User;
import com.example.Shorty.user.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.endpoints.internal.Value;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public UserResponse registerUser(RegisterRequest request) throws BadRequestException {
        if (userRepo.existsByEmail(request.getEmail())) {
            throw new BadRequestException("User with email " + request.getEmail() + " already exists");
        }

        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .apiKey(generateApiKey())
                .isActive(true)
                .role(Role.USER)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();


        User savedUser = userRepo.saveUser(user);

        return mapToUserResponse(user);

    }

    public AuthResponse loginUser(LoginRequest request) throws BadRequestException, ResourceNotFoundException {

        User user = userRepo.findByEmail(request.getEmail()).orElseThrow(
                () -> new ResourceNotFoundException("User With Email Not Found")
        );

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid Credential");
        }

        if(!user.isActive()) {
            throw new BadRequestException("User Account Is Not Active");
        }

        String token = jwtUtils.generateToken(user.getId(),user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .build();
    }

    public String generateApiKey() {
        return "Sk_" + UUID.randomUUID().toString().replace("-", "");
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .apiKey(user.getApiKey())
                .active(user.isActive())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

}
