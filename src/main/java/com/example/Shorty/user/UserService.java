package com.example.Shorty.user;


import com.example.Shorty.DTOs.UserDtos.CredentialsRequest;
import com.example.Shorty.DTOs.UserDtos.RegisterRequest;
import com.example.Shorty.DTOs.UserDtos.UserResponse;
import com.example.Shorty.Utils.JwtUtils;
import com.example.Shorty.exception.UnauthorizedException;
import com.example.Shorty.exception.BadRequestException;
import com.example.Shorty.exception.ResourceNotFoundException;
import com.example.Shorty.security.CookieBuilder;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final CookieBuilder cookieBuilder;

    public String registerUser(RegisterRequest request) {
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
        return "done";
    }

    public void loginUser(CredentialsRequest request, HttpServletResponse response) {

        User user = validateUser(request);
        String token = jwtUtils.generateToken(user.getId(), user.getEmail());

        cookieBuilder.setJwtCookie(response, token);
    }


    public String deactivateUser(CredentialsRequest request) {

        User user = validateUser(request);

        user.setActive(false);
        userRepo.saveUser(user);

        return "User deactivated successfully";
    }

    private User validateUser(CredentialsRequest request) {

        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User with email not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid credentials");
        }

        if (!user.isActive()) {
            throw new BadRequestException("User account is not active");
        }

        return user;
    }


    public String generateApiKey() {
        return "Sk_" + UUID.randomUUID().toString().replace("-", "");
    }

    public UserResponse getUserById(String userId) {
        User user = userRepo.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return mapToUserResponse(user);
    }

    public void logout(HttpServletResponse response) {
        cookieBuilder.logoutCookie(response);
    }

    public String getUserIdFromSecurityContext() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new UnauthorizedException("Unauthorized");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails user)) {
            throw new UnauthorizedException("Invalid authentication");
        }

        return user.getUserId();
    }



    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
//                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
//                .apiKey(user.getApiKey())
                .active(user.isActive())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

}
