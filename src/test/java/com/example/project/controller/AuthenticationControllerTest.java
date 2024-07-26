package com.example.project.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.project.dto.user.UserLoginRequestDto;
import com.example.project.dto.user.UserLoginResponseDto;
import com.example.project.dto.user.UserRegistrationRequestDto;
import com.example.project.dto.user.UserRegistrationResponseDto;
import com.example.project.repository.UserRepository;
import com.example.project.security.AuthenticationService;
import com.example.project.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AuthenticationControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserService userService;

    @Autowired
    UserRepository userRepository;

    @Test
    @Sql(scripts = "classpath:database/users/delete-test-user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/users/delete-test-user.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Register new user is successful")
    void registerUser_ValidRequestDto_Success() throws Exception {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("testuser@test.com");
        requestDto.setPassword("password");
        requestDto.setFirstName("Test");
        requestDto.setLastName("User");

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        UserRegistrationResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), UserRegistrationResponseDto.class);

        assertNotNull(actual);
        assertNotNull(actual.getId());
        assertEquals(requestDto.getEmail(), actual.getEmail());
        assertEquals(requestDto.getFirstName(), actual.getFirstName());
        assertEquals(requestDto.getLastName(), actual.getLastName());
    }

    @Test
    @Sql(scripts = "classpath:database/users/add-test-user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/users/delete-test-user.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Register user with existing email is Conflict")
    void registerUser_ExistingEmail_ReturnsConflict() throws Exception {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("testuser@test.com");
        requestDto.setPassword("password");
        requestDto.setFirstName("Customer");
        requestDto.setLastName("Test");

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(post("/api/auth/register")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Register user with invalid RequestDto is Bad Request")
    void registerUser_InValidRequestDto_BadRequest() throws Exception {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("testuser@test.com");
        requestDto.setPassword("password");

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(post("/api/auth/register")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(scripts = "classpath:database/users/add-test-user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/users/delete-test-user.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Login user is successful")
    void loginUser_ValidRequestDto_Success() throws Exception {
        UserLoginRequestDto requestDto = new UserLoginRequestDto();
        requestDto.setEmail("testuser@test.com");
        requestDto.setPassword("password");

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        UserLoginResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), UserLoginResponseDto.class);

        assertNotNull(actual);
        assertEquals(25L, actual.userId());
        assertNotNull(actual.token());
    }

    @Test
    @DisplayName("Login with invalid credentials returns Unauthorized")
    void loginUser_InvalidCredentials_ReturnsUnauthorized() throws Exception {
        UserLoginRequestDto requestDto = new UserLoginRequestDto();
        requestDto.setEmail("invaliduser@test.com");
        requestDto.setPassword("wrongpassword");

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(post("/api/auth/login")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
