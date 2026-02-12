package com.example.Shorty.user;



import com.example.Shorty.DTOs.ApiResponse;
import com.example.Shorty.DTOs.UserDtos.AuthResponse;
import com.example.Shorty.DTOs.UserDtos.CredentialsRequest;
import com.example.Shorty.DTOs.UserDtos.RegisterRequest;
import com.example.Shorty.DTOs.UserDtos.UserResponse;
import com.example.Shorty.exception.BadRequestException;
import com.example.Shorty.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {


    private final UserService userService;


    @GetMapping("/")
    public String test() {
        return "HOME";
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> currentUser() {

        String userId = userService.getUserIdFromSecurityContext();

        if (userId == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized"));
        }

        UserResponse user = userService.getUserById(userId);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(user));
    }



    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        UserResponse response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody CredentialsRequest request, HttpServletResponse response) {

        userService.loginUser(request, response);
        return ResponseEntity.status(HttpStatus.OK).body(AuthResponse.builder().message("Login successful").build());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        userService.logout(response);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/delete")
    public ResponseEntity<String> delete(
            @Valid @RequestBody CredentialsRequest authRequest) throws BadRequestException, ResourceNotFoundException {

        String response = userService.deactivateUser(authRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }

}
