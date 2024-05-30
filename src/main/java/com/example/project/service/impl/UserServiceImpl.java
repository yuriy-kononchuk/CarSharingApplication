package com.example.project.service.impl;

import com.example.project.dto.user.UserDto;
import com.example.project.dto.user.UserRegistrationRequestDto;
import com.example.project.dto.user.UserRegistrationResponseDto;
import com.example.project.dto.user.UserRoleRequestDto;
import com.example.project.exception.EntityNotFoundException;
import com.example.project.exception.RegistrationException;
import com.example.project.mapper.UserMapper;
import com.example.project.model.Role;
import com.example.project.model.User;
import com.example.project.repository.RoleRepository;
import com.example.project.repository.UserRepository;
import com.example.project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    public UserRegistrationResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new RegistrationException("Can't register a user, the user already exists");
        }
        User user = userMapper.toEntity(requestDto);
        //user.getRoles().add(new Role(Role.RoleName.CUSTOMER)); // GENERAL OPTION
        user.getRoles().add(new Role(Role.RoleName.MANAGER)); // fpr Testing
        return userMapper.toUserResponseDto(userRepository.save(user));
    }

    @Override
    public UserDto getProfileByUserId(Long userId) {
        User userById = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("Can't find a user's profile with id: "
                        + userId));
        return userMapper.toDto(userById);
    }

    @Override
    @Transactional
    public UserDto updateProfileByUserId(Long userId, UserRegistrationRequestDto requestDto) {
        User userToUpdate = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("Can't find a user's profile with id: "
                        + userId));
        userToUpdate.setEmail(requestDto.getEmail());
        userToUpdate.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        userToUpdate.setFirstName(requestDto.getFirstName());
        userToUpdate.setLastName(requestDto.getLastName());
        return userMapper.toDto(userRepository.save(userToUpdate));
    }

    @Override
    @Transactional
    public UserDto updateUserRole(Long userId, UserRoleRequestDto requestDto) {
        User userToUpdate = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("Can't find a user by id: " + userId));
        Role roleToUpdate = roleRepository.save(new Role(requestDto.roleName()));
        userToUpdate.getRoles().clear();
        userToUpdate.getRoles().add(roleToUpdate);
        return userMapper.toDto(userRepository.save(userToUpdate));
    }
}
