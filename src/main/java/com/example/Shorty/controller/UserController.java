package com.example.Shorty.controller;



import com.example.Shorty.DTOs.UserDtos.AuthResponse;
import com.example.Shorty.DTOs.UserDtos.RegisterRequest;
import com.example.Shorty.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.function.EntityResponse;

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
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) throws BadRequestException {

        AuthResponse response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

}
