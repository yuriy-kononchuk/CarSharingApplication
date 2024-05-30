package com.example.project.controller;

import com.example.project.dto.user.UserDto;
import com.example.project.dto.user.UserRegistrationRequestDto;
import com.example.project.dto.user.UserRoleRequestDto;
import com.example.project.model.User;
import com.example.project.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User management", description = "Endpoints for mapping users")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PreAuthorize("hasAuthority('MANAGER')")
    @PutMapping("/{id}")
    @Operation(summary = "Update user's role by id", description = "Update user's role by id")
    @ApiResponse(responseCode = "200", description = "Requested user's role was updated")
    public UserDto updateRole(@RequestBody UserRoleRequestDto requestDto, @PathVariable Long id) {
        return userService.updateUserRole(id, requestDto);
    }

    @GetMapping("/me")
    @Operation(summary = "Get a user's profile", description = "Get a user's profile")
    @ApiResponse(responseCode = "200", description = "Got a user's current profile")
    public UserDto getProfile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return userService.getProfileByUserId(user.getId());
    }

    @PutMapping("/me")
    @Operation(summary = "Update a user's profile", description = "Update a user's profile")
    @ApiResponse(responseCode = "200", description = "Requested user's profile was updated")
    public UserDto updateProfile(Authentication authentication,
                                 @RequestBody @Valid UserRegistrationRequestDto requestDto) {
        User user = (User) authentication.getPrincipal();
        return userService.updateProfileByUserId(user.getId(), requestDto);
    }

}
