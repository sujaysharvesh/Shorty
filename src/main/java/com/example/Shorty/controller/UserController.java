package com.example.Shorty.controller;



import com.example.Shorty.DTOs.UserDtos.AuthResponse;
import com.example.Shorty.DTOs.UserDtos.LoginRequest;
import com.example.Shorty.DTOs.UserDtos.RegisterRequest;
import com.example.Shorty.DTOs.UserDtos.UserResponse;
import com.example.Shorty.exception.BadRequestException;
import com.example.Shorty.exception.ResourceNotFoundException;
import com.example.Shorty.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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


    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request) throws BadRequestException {

        UserResponse response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) throws BadRequestException, ResourceNotFoundException {

        AuthResponse response = userService.loginUser(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
