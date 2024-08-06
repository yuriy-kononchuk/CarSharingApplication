package com.example.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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
import com.example.project.service.impl.UserServiceImpl;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RoleRepository roleRepository;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Verify register() method register a new user successfully")
    void register_NewUser_Success() throws RegistrationException {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("test@example.com");
        requestDto.setPassword("password");
        requestDto.setFirstName("User");
        requestDto.setLastName("First");

        User user = new User();
        user.setEmail(requestDto.getEmail());
        user.setPassword("encoded_password");
        user.setFirstName(requestDto.getFirstName());
        user.setLastName(requestDto.getLastName());

        UserRegistrationResponseDto responseDto = new UserRegistrationResponseDto();
        responseDto.setId(1L);
        responseDto.setEmail(user.getEmail());
        responseDto.setFirstName(user.getFirstName());
        responseDto.setLastName(user.getLastName());

        when(userRepository.findByEmail(requestDto.getEmail())).thenReturn(Optional.empty());
        when(userMapper.toEntity(requestDto)).thenReturn(user);
        when(userMapper.toUserResponseDto(user)).thenReturn(responseDto);
        when(userRepository.save(user)).thenReturn(user);

        UserRegistrationResponseDto actualDto = userService.register(requestDto);

        assertNotNull(actualDto);
        assertEquals(responseDto.getId(), actualDto.getId());
        assertEquals(responseDto.getEmail(), actualDto.getEmail());
        assertEquals(responseDto.getFirstName(), actualDto.getFirstName());
        assertEquals(responseDto.getLastName(), actualDto.getLastName());

        verify(userRepository, times(1)).findByEmail(requestDto.getEmail());
        verify(userMapper, times(1)).toEntity(requestDto);
        verify(userRepository, times(1)).save(user);
        verify(userMapper, times(1)).toUserResponseDto(user);
        verifyNoMoreInteractions(userRepository, userMapper, passwordEncoder);
    }

    @Test
    @DisplayName("Verify to fail to register a new user if already exists")
    void register_UserExistsWithEmail_ThrowsException() {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("test@example.com");
        requestDto.setPassword("password");
        requestDto.setFirstName("User");
        requestDto.setLastName("First");

        User existingUser = new User();
        existingUser.setEmail(requestDto.getEmail());

        when(userRepository.findByEmail(
                requestDto.getEmail())).thenReturn(Optional.of(existingUser));

        Exception exception = assertThrows(RegistrationException.class,
                () -> userService.register(requestDto));

        assertEquals("Can't register a user, the user already exists", exception.getMessage());

        verify(userRepository, times(1)).findByEmail(requestDto.getEmail());
        verifyNoMoreInteractions(userRepository, userMapper, passwordEncoder);
    }

    @Test
    @DisplayName("Verify getProfileByUserId() method get user's profile by user ID")
    void getProfileByUserId_ValidId_ReturnsUserTo() {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setFirstName("User");
        user.setLastName("First");

        UserDto expecetedUserDto = new UserDto();
        expecetedUserDto.setId(user.getId());
        expecetedUserDto.setEmail(user.getEmail());
        expecetedUserDto.setFirstName(user.getFirstName());
        expecetedUserDto.setLastName(user.getLastName());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(expecetedUserDto);

        UserDto actualUserDto = userService.getProfileByUserId(userId);

        assertNotNull(actualUserDto);
        assertEquals(expecetedUserDto, actualUserDto);
        assertEquals(user.getId(), actualUserDto.getId());
        assertEquals(user.getEmail(), actualUserDto.getEmail());
        assertEquals(user.getFirstName(), actualUserDto.getFirstName());
        assertEquals(user.getLastName(), actualUserDto.getLastName());

        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, times(1)).toDto(user);
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    @DisplayName("Verify getProfileByUserId by invalid user's Id throws exception")
    void getProfileByUserId_InvalidUserId_ThrowsException() {
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> userService.getProfileByUserId(userId));

        assertEquals("Can't find a user's profile with id: " + userId, exception.getMessage());

        verify(userRepository, times(1)).findById(userId);
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    @DisplayName("Verify updateProfileByUserId() updates user's profile by Id and returns UserDto")
    void updateProfileByUserId_ValidUserId_ReturnsUpdatedUserDto() {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("new_email@example.com");
        requestDto.setPassword("new_password");
        requestDto.setFirstName("Updated");
        requestDto.setLastName("User");

        Long userId = 1L;

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setEmail("test@example.com");
        existingUser.setFirstName("User");
        existingUser.setLastName("First");

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setEmail(requestDto.getEmail());
        updatedUser.setPassword("encoded_password");
        updatedUser.setFirstName(requestDto.getFirstName());
        updatedUser.setLastName(requestDto.getLastName());

        UserDto updatedUserDto = new UserDto();
        updatedUserDto.setId(updatedUser.getId());
        updatedUserDto.setEmail(updatedUser.getEmail());
        updatedUserDto.setFirstName(updatedUser.getFirstName());
        updatedUserDto.setLastName(updatedUser.getLastName());

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode(requestDto.getPassword())).thenReturn("encoded_password");
        when(userRepository.save(updatedUser)).thenReturn(updatedUser);
        when(userMapper.toDto(updatedUser)).thenReturn(updatedUserDto);

        UserDto actualUserDto = userService.updateProfileByUserId(userId, requestDto);

        assertNotNull(actualUserDto);
        assertEquals(updatedUser.getId(), actualUserDto.getId());
        assertEquals(updatedUser.getEmail(), actualUserDto.getEmail());
        assertEquals(updatedUser.getFirstName(), actualUserDto.getFirstName());
        assertEquals(updatedUser.getLastName(), actualUserDto.getLastName());

        verify(userRepository, times(1)).findById(userId);
        verify(passwordEncoder, times(1)).encode(requestDto.getPassword());
        verify(userRepository, times(1)).save(updatedUser);
        verify(userMapper, times(1)).toDto(updatedUser);
        verifyNoMoreInteractions(userRepository, passwordEncoder, userMapper);
    }

    @Test
    @DisplayName("Verify updateProfileByUserId() with invalid user Id throws exception")
    void updateProfileByUserId_InvalidUserId_ThrowsException() {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("new_email@example.com");
        requestDto.setPassword("new_password");
        requestDto.setFirstName("Updated");
        requestDto.setLastName("User");

        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> userService.updateProfileByUserId(userId, requestDto));

        assertEquals("Can't find a user's profile with id: " + userId, exception.getMessage());

        verify(userRepository, times(1)).findById(userId);
        verifyNoMoreInteractions(userRepository, passwordEncoder, userMapper);
    }

    @Test
    @DisplayName("Verify updateUserRole() method updates user's role successfully")
    void updateUserRole_ValidUserId_ReturnsUpdatedUserDto() {
        Long userId = 1L;

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setEmail("test@example.com");
        existingUser.setFirstName("User");
        existingUser.setLastName("Last");
        existingUser.getRoles().add(new Role(Role.RoleName.CUSTOMER));

        Role roleToUpdate = new Role(Role.RoleName.MANAGER);

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.getRoles().add(roleToUpdate);
        updatedUser.setEmail("test@example.com");
        updatedUser.setFirstName("User");
        updatedUser.setLastName("Last");

        UserDto updatedUserDto = new UserDto();
        updatedUserDto.setId(updatedUser.getId());
        updatedUserDto.setEmail(updatedUser.getEmail());
        updatedUserDto.setFirstName(updatedUser.getFirstName());
        updatedUserDto.setLastName(updatedUser.getLastName());
        updatedUserDto.setRoleNames(Set.of(roleToUpdate.getName()));

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(roleRepository.save(any(Role.class))).thenReturn(roleToUpdate);
        when(userRepository.save(existingUser)).thenReturn(updatedUser);
        when(userMapper.toDto(updatedUser)).thenReturn(updatedUserDto);

        UserRoleRequestDto requestDto = new UserRoleRequestDto(Role.RoleName.MANAGER);
        UserDto actualUserDto = userService.updateUserRole(userId, requestDto);

        assertNotNull(actualUserDto);
        assertEquals(updatedUser.getId(), actualUserDto.getId());
        assertEquals(updatedUser.getEmail(), actualUserDto.getEmail());
        assertEquals(updatedUser.getFirstName(), actualUserDto.getFirstName());
        assertEquals(updatedUser.getLastName(), actualUserDto.getLastName());
        assertTrue(actualUserDto.getRoleNames().contains(Role.RoleName.MANAGER));

        verify(userRepository, times(1)).findById(userId);
        verify(roleRepository, times(1)).save(any(Role.class));
        verify(userRepository, times(1)).save(existingUser);
        verify(userMapper, times(1)).toDto(updatedUser);
        verifyNoMoreInteractions(userRepository, roleRepository, userMapper);
    }

    @Test
    @DisplayName("Verify updateUserRole() method throws exception with wrong user's Id")
    void updateUserRole_InvalidUserId_ThrowsException() {
        Long userId = 1L;

        UserRoleRequestDto requestDto = new UserRoleRequestDto(Role.RoleName.MANAGER);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> userService.updateUserRole(userId, requestDto));

        assertEquals("Can't find a user by id: " + userId, exception.getMessage());

        verify(userRepository, times(1)).findById(userId);
        verifyNoMoreInteractions(userRepository, roleRepository, userMapper);
    }
}

