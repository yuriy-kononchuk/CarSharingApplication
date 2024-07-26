package com.example.project.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.project.dto.user.UserDto;
import com.example.project.dto.user.UserRegistrationRequestDto;
import com.example.project.dto.user.UserRoleRequestDto;
import com.example.project.model.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Sql(scripts = "classpath:database/users/add-test-users-cars-set.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/users/delete-test-users-cars-set.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @WithUserDetails("manager@test.com")
    @Test
    @DisplayName("Update user's role by id with MANAGER authority is successful")
    void updateUserRole_WithManagerAuthority_Success() throws Exception {
        Long userId = 5L;
        UserRoleRequestDto requestDto = new UserRoleRequestDto(Role.RoleName.CUSTOMER);

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(put("/users/" + userId)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        UserDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), UserDto.class);

        assertNotNull(actual);
        assertNotNull(actual.getId());
        assertEquals(requestDto.roleName(), actual.getRoleNames().iterator().next());
    }

    @WithUserDetails("testuser@test.com")
    @Test
    @DisplayName("Update user's role by id without MANAGER authority is Forbidden")
    void updateUserRole_WithoutManagerAuthority_Forbidden() throws Exception {
        Long userId = 5L;
        UserRoleRequestDto requestDto = new UserRoleRequestDto(Role.RoleName.CUSTOMER);

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(put("/users/" + userId)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @WithUserDetails("manager@test.com")
    @Test
    @DisplayName("Update user's role by invalid user is Not Found")
    void updateUserRole_IvalidUserId_ReturnsNotFound() throws Exception {
        Long userId = 100L;
        UserRoleRequestDto requestDto = new UserRoleRequestDto(Role.RoleName.CUSTOMER);

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(put("/users/" + userId)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @WithUserDetails("testuser@test.com")
    @Test
    @DisplayName("Get profile of the authenticated user is successful")
    void getProfile_ValidAuthenticatedUser_Success() throws Exception {
        MvcResult result = mockMvc.perform(get("/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        UserDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), UserDto.class);

        assertNotNull(actual);
        assertEquals(5L, actual.getId());
        assertEquals("testuser@test.com", actual.getEmail());
        assertEquals("customer", actual.getFirstName());
        assertEquals("test", actual.getLastName());
    }

    @Test
    @DisplayName("Get profile without authentication is Unauthorized")
    void getProfile_UnauthenticatedUser_Forbidden() throws Exception {
        mockMvc.perform(get("/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @WithUserDetails("testuser@test.com")
    @Test
    @DisplayName("Update profile of the authenticated user is successful")
    void updateProfile_ValidAuthenticatedUser_Success() throws Exception {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("newemail@test.com");
        requestDto.setPassword("newpassword");
        requestDto.setFirstName("NewFirstName");
        requestDto.setLastName("NewLastName");

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(put("/users/me")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        UserDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), UserDto.class);

        assertNotNull(actual);
        assertEquals(5L, actual.getId());
        assertEquals("newemail@test.com", actual.getEmail());
        assertEquals("NewFirstName", actual.getFirstName());
        assertEquals("NewLastName", actual.getLastName());
    }

    @WithUserDetails("testuser@test.com")
    @Test
    @DisplayName("Update profile with invalid email is Bad Request")
    void updateProfile_InvalidEmail_ReturnsBadRequest() throws Exception {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("invalid-email");
        requestDto.setPassword("newpassword");
        requestDto.setFirstName("NewFirstName");
        requestDto.setLastName("NewLastName");

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(put("/users/me")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @WithUserDetails("testuser@test.com")
    @Test
    @DisplayName("Update profile with short password is Bad Request")
    void updateProfile_ShortPassword_ReturnsBadRequest() throws Exception {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("newemail@test.com");
        requestDto.setPassword("newpsw");
        requestDto.setFirstName("NewFirstName");
        requestDto.setLastName("NewLastName");

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(put("/users/me")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
