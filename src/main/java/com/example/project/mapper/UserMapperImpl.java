package com.example.project.mapper;

import com.example.project.dto.user.UserDto;
import com.example.project.dto.user.UserRegistrationRequestDto;
import com.example.project.dto.user.UserRegistrationResponseDto;
import com.example.project.model.Rental;
import com.example.project.model.Role;
import com.example.project.model.User;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapperImpl implements UserMapper {
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserRegistrationResponseDto toUserResponseDto(User user) {
        if (user == null) {
            return null;
        }
        UserRegistrationResponseDto userDto = new UserRegistrationResponseDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        return userDto;
    }

    @Override
    public User toEntity(UserRegistrationRequestDto requestDto) {
        if (requestDto == null) {
            return null;
        }
        User user = new User();
        user.setEmail(requestDto.getEmail());
        user.setFirstName(requestDto.getFirstName());
        user.setLastName(requestDto.getLastName());
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        return user;
    }

    @Override
    public UserDto toDto(User user) { //Looks the same?
        if (user == null) {
            return null;
        }
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());

        Set<Role.RoleName> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        userDto.setRoleNames(roleNames);

        List<Long> rentalIds = user.getRentals().stream()
                .map(Rental::getId)
                .toList();
        userDto.setRentalIds(rentalIds);

        return userDto;
    }


}
