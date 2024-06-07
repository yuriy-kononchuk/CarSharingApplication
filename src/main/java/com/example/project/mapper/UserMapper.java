package com.example.project.mapper;

import com.example.project.dto.user.UserDto;
import com.example.project.dto.user.UserRegistrationRequestDto;
import com.example.project.dto.user.UserRegistrationResponseDto;
import com.example.project.model.User;

public interface UserMapper {
    UserRegistrationResponseDto toUserResponseDto(User user);

    User toEntity(UserRegistrationRequestDto requestDto);

    UserDto toDto(User user);
}
