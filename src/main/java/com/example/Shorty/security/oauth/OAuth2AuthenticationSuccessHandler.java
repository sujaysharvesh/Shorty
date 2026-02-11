package com.example.Shorty.security.oauth;


import com.example.Shorty.Utils.JwtUtils;
import com.example.Shorty.security.CookieBuilder;
import com.example.Shorty.user.Provider;
import com.example.Shorty.user.Role;
import com.example.Shorty.user.User;
import com.example.Shorty.user.UserRepo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepo userRepo;
    private final JwtUtils jwtUtils;
    private final CookieBuilder cookieBuilder;

    @Value("${oauth2.redirect-uri}")
    private String redirectUri;

    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = oauth2Token.getPrincipal();
            String registrationId = oauth2Token.getAuthorizedClientRegistrationId();

            User user = processOAuth2User(registrationId, oauth2User);

            String accessToken = jwtUtils.generateToken(user.getId(), user.getEmail());

            // Set cookies
            cookieBuilder.setJwtCookie(response, accessToken);

            // Redirect to frontend
            getRedirectStrategy().sendRedirect(request, response, redirectUri);
        }
    }

    private User processOAuth2User(String provider, OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();

        String email = extractEmail(provider, attributes);
        String name = extractName(provider, attributes);
        String providerId = extractProviderId(provider, attributes);
        String profileImageUrl = extractProfileImageUrl(provider, attributes);

        Provider actualProvider = switch (provider.toUpperCase()) {
            case "GOOGLE" -> Provider.GOOGLE;
            case "GITHUB" -> Provider.GITHUB;
            default -> throw new IllegalArgumentException("Unsupported provider");
        };


        // Check if user already exists
        return userRepo.findByEmail(email)
                .map(existingUser -> {
                    // Update existing user
                    existingUser.setProvider(actualProvider);
                    existingUser.setProviderId(providerId);
                    existingUser.setUpdatedAt(Instant.now());
                    return userRepo.saveUser(existingUser);
                })
                .orElseGet(() -> {
                    // Create new user
                    User newUser = User.builder()
                            .id(UUID.randomUUID().toString())
                            .email(email)
                            .username(name)
                            .password(null)
                            .apiKey(generateApiKey())
                            .isActive(true)
                            .role(Role.USER)
                            .provider(actualProvider)
                            .providerId(providerId)
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build();

                    return userRepo.saveUser(newUser);
                });
    }

    private String extractEmail(String provider, Map<String, Object> attributes) {
        switch (provider.toLowerCase()) {
            case "google":
                return (String) attributes.get("email");
            case "github":
                // GitHub might not provide email if it's private
                String email = (String) attributes.get("email");
                if (email == null) {
                    // Fallback to login@github.com
                    String login = (String) attributes.get("login");
                    email = login + "@github.user";
                }
                return email;
            default:
                return (String) attributes.get("email");
        }
    }

    private String extractName(String provider, Map<String, Object> attributes) {
        switch (provider.toLowerCase()) {
            case "google":
                return (String) attributes.get("name");
            case "github":
                String name = (String) attributes.get("name");
                return name != null ? name : (String) attributes.get("login");
            default:
                return (String) attributes.get("name");
        }
    }

    private String extractProviderId(String provider, Map<String, Object> attributes) {
        switch (provider.toLowerCase()) {
            case "google":
                return (String) attributes.get("sub");
            case "github":
                Object id = attributes.get("id");
                return id != null ? id.toString() : null;
            default:
                return (String) attributes.get("id");
        }
    }

    private String extractProfileImageUrl(String provider, Map<String, Object> attributes) {
        switch (provider.toLowerCase()) {
            case "google":
                return (String) attributes.get("picture");
            case "github":
                return (String) attributes.get("avatar_url");
            default:
                return null;
        }
    }

    private String generateApiKey() {
        return "sk_" + UUID.randomUUID().toString().replace("-", "");
    }

}
