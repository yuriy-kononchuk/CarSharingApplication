package com.example.project.controller;

import com.example.project.dto.user.UserLoginRequestDto;
import com.example.project.dto.user.UserLoginResponseDto;
import com.example.project.dto.user.UserRegistrationRequestDto;
import com.example.project.dto.user.UserRegistrationResponseDto;
import com.example.project.exception.RegistrationException;
import com.example.project.security.AuthenticationService;
import com.example.project.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User authentication management",
        description = "Endpoints for mapping user's authentication")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Register a new user")
    @ApiResponse(responseCode = "201", description = "New user is successfully registered")
    public UserRegistrationResponseDto registerUser(@RequestBody @Valid UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        return userService.register(requestDto);
    }

    @PostMapping("/login")
    @Operation(summary = "Provides ability to login a user", description = "Login a user")
    @ApiResponse(responseCode = "200", description = "User is successfully logged in")
    public UserLoginResponseDto login(@RequestBody @Valid UserLoginRequestDto request) {
        return authenticationService.authentificate(request);
    }
}
