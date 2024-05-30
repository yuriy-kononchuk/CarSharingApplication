package com.example.project.service;

import com.example.project.dto.user.UserDto;
import com.example.project.dto.user.UserRegistrationRequestDto;
import com.example.project.dto.user.UserRegistrationResponseDto;
import com.example.project.dto.user.UserRoleRequestDto;
import com.example.project.exception.RegistrationException;

public interface UserService {
    UserRegistrationResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException;

    UserDto updateUserRole(Long userId, UserRoleRequestDto requestDto);

    UserDto getProfileByUserId(Long userId);

    UserDto updateProfileByUserId(Long userId, UserRegistrationRequestDto requestDto);

}
