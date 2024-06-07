package com.example.project.security;

import com.example.project.dto.user.UserLoginRequestDto;
import com.example.project.dto.user.UserLoginResponseDto;
import com.example.project.exception.EntityNotFoundException;
import com.example.project.model.User;
import com.example.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    public UserLoginResponseDto authentificate(UserLoginRequestDto requestDto) {
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestDto.getEmail(),
                        requestDto.getPassword()));
        String token = jwtUtil.generateToken(authentication.getName());
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: "
                        + requestDto.getEmail()));
        return new UserLoginResponseDto(user.getId(), token);
    }
}


