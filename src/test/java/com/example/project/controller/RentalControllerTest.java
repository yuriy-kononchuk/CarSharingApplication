package com.example.project.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.project.dto.rental.CreateRentalRequestDto;
import com.example.project.dto.rental.RentalDto;
import com.example.project.model.Car;
import com.example.project.model.Rental;
import com.example.project.repository.CarRepository;
import com.example.project.repository.RentalRepository;
import com.example.project.service.ApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class RentalControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RentalRepository rentalRepository;
    @Autowired
    private CarRepository carRepository;

    @MockBean
    private ApiService apiService;

    @WithUserDetails("testuser@test.com")
    @Test
    @Sql(scripts = "classpath:database/users/add-test-users-cars-set.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/users/delete-test-users-cars-set.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Create new rental is successful")
    void createRental_ValidRequestDto_Success() throws Exception {
        CreateRentalRequestDto requestDto = new CreateRentalRequestDto(
                1L, LocalDate.now().plusDays(3), LocalDate.now().plusDays(5));

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(post("/rentals")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        RentalDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), RentalDto.class);

        assertNotNull(actual);
        assertNotNull(actual.getId());
        assertEquals(requestDto.carId(), actual.getCarId());
        assertEquals(requestDto.rentalDate(), actual.getRentalDate());
        assertEquals(requestDto.returnDate(), actual.getReturnDate());
    }

    @Test
    @WithUserDetails("testuser@test.com")
    @Sql(scripts = "classpath:database/users/add-test-users-cars-set.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/users/delete-test-users-cars-set.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Create rental with invalid car Id Id returns Not Found")
    void createRental_InvalidCarId__ReturnsNotFound() throws Exception {
        CreateRentalRequestDto requestDto = new CreateRentalRequestDto(
                100L, LocalDate.now().plusDays(3), LocalDate.now().plusDays(5));
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(post("/rentals")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithUserDetails("testuser@test.com")
    @Sql(scripts = "classpath:database/users/add-test-users-cars-set.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/users/delete-test-users-cars-set.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Create rental with past rental date is Bad Request")
    void createRental_PastRentalDate_ReturnsBadRequest() throws Exception {
        CreateRentalRequestDto requestDto = new CreateRentalRequestDto(
                1L, LocalDate.now().minusDays(1), LocalDate.now().plusDays(5));

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(post("/rentals")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails("testuser@test.com")
    @Sql(scripts = "classpath:database/users/add-test-users-cars-set.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/users/delete-test-users-cars-set.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Create rental with past return date is Bad Request")
    void createRental_PastReturnDate_ReturnsBadRequest() throws Exception {
        CreateRentalRequestDto requestDto = new CreateRentalRequestDto(
                1L, LocalDate.now().plusDays(3), LocalDate.now().minusDays(1));
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(post("/rentals")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails("testuser@test.com")
    @Sql(scripts = "classpath:database/users/add-test-users-cars-set.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/users/delete-test-users-cars-set.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Create rental with rental date after return date is Bad Request")
    void createRental_RentalDateAfterReturnDate_ReturnsBadRequest() throws Exception {
        CreateRentalRequestDto requestDto = new CreateRentalRequestDto(
                1L, LocalDate.now().plusDays(5), LocalDate.now().plusDays(3));

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(post("/rentals")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @WithUserDetails("testuser@test.com")
    @Test
    @Sql(scripts = "classpath:database/users/add-test-users-cars-set.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/users/delete-test-users-cars-set.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Get rental by Id is successful")
    void getById_ValidUserAndRentalId_Success() throws Exception {
        Long rentalId = 1L;
        MvcResult result = mockMvc.perform(get("/rentals/" + rentalId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        RentalDto actualDto = objectMapper.readValue(
                result.getResponse().getContentAsString(), RentalDto.class);

        assertNotNull(actualDto);
        assertEquals(1L, actualDto.getId());
        assertEquals(5L, actualDto.getUserId());
        assertEquals(1L, actualDto.getCarId());
        assertEquals(LocalDate.of(2024, 3, 1), actualDto.getRentalDate());
        assertEquals(LocalDate.of(2024, 3, 10), actualDto.getReturnDate());
        assertEquals(LocalDate.of(2024, 3, 9), actualDto.getActualReturnDate());
        assertEquals("true", actualDto.getIsActive());
    }

    @WithUserDetails("testuser@test.com")
    @Test
    @Sql(scripts = "classpath:database/users/add-test-users-cars-set.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/users/delete-test-users-cars-set.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Get rental by invalid Id returns Not Found")
    void getById_ValidUserInvalidRentalId_BadRequest() throws Exception {
        Long invalidRentalId = 100L;
        mockMvc.perform(get("/rentals/" + invalidRentalId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(scripts = "classpath:database/users/add-test-users-cars-set.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/users/delete-test-users-cars-set.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Get rental by Id with user Not Authenticated")
    void getById_NotAuthenticated_Unauthorized() throws Exception {
        Long rentalId = 1L;
        mockMvc.perform(get("/rentals/" + rentalId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @WithUserDetails("testuser@test.com")
    @Test
    @Sql(scripts = "classpath:database/users/add-test-users-cars-set.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/users/delete-test-users-cars-set.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Get all active rentals for user CUSTOMER is successful")
    void getAllRentalsByUserAndIsActive_UserIsCustomer_Success() throws Exception {
        Long userId = 5L;
        boolean isActive = true;

        RentalDto activeRentalDto1 = new RentalDto();
        activeRentalDto1.setId(1L);
        activeRentalDto1.setRentalDate(LocalDate.of(2024, 3, 1));
        activeRentalDto1.setReturnDate(LocalDate.of(2024, 3, 10));
        activeRentalDto1.setActualReturnDate(LocalDate.of(2024, 3, 9));
        activeRentalDto1.setCarId(1L);
        activeRentalDto1.setUserId(5L);
        activeRentalDto1.setIsActive("true");

        RentalDto activeRentalDto2 = new RentalDto();
        activeRentalDto2.setId(2L);
        activeRentalDto2.setRentalDate(LocalDate.of(2024, 2, 1));
        activeRentalDto2.setReturnDate(LocalDate.of(2024, 2, 10));
        activeRentalDto2.setActualReturnDate(LocalDate.of(2024, 2, 9));
        activeRentalDto2.setCarId(2L);
        activeRentalDto2.setUserId(5L);
        activeRentalDto2.setIsActive("true");

        List<RentalDto> expected = new ArrayList<>();
        expected.add(activeRentalDto1);
        expected.add(activeRentalDto2);

        MvcResult result = mockMvc.perform(get("/rentals")
                        .param("userId", userId.toString())
                        .param("isActive", Boolean.toString(isActive))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        RentalDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), RentalDto[].class);
        assertEquals(expected.size(), actual.length);
        assertEquals(expected, Arrays.stream(actual).toList());
    }

    @WithUserDetails("testuser@test.com")
    @Test
    @Sql(scripts = "classpath:database/users/add-test-users-cars-set.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/users/delete-test-users-cars-set.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Get all inactive rentals for user CUSTOMER is successful")
    void getAllRentalsByUserAndIsInActive_UserIsCustomer_Success() throws Exception {
        Long userId = 5L;
        boolean isActive = false;

        RentalDto inactiveRentalDto = new RentalDto();
        inactiveRentalDto.setId(3L);
        inactiveRentalDto.setRentalDate(LocalDate.of(2024, 1, 1));
        inactiveRentalDto.setReturnDate(LocalDate.of(2024, 1, 5));
        inactiveRentalDto.setActualReturnDate(LocalDate.of(2024, 1, 5));
        inactiveRentalDto.setCarId(3L);
        inactiveRentalDto.setUserId(5L);
        inactiveRentalDto.setIsActive("false");

        List<RentalDto> expected = new ArrayList<>();
        expected.add(inactiveRentalDto);

        MvcResult result = mockMvc.perform(get("/rentals")
                        .param("userId", userId.toString())
                        .param("isActive", Boolean.toString(isActive))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        RentalDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), RentalDto[].class);
        assertEquals(expected.size(), actual.length);
        assertEquals(expected, Arrays.stream(actual).toList());
    }

    @WithUserDetails("manager@test.com")
    @Test
    @Sql(scripts = "classpath:database/users/add-test-users-cars-set.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/users/delete-test-users-cars-set.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Get all active rentals for user MANAGER is successful")
    void getAllRentalsByUserAndIsActive_UserIsManager_Success() throws Exception {
        Long userId = 6L;
        boolean isActive = true;

        RentalDto activeRentalDto = new RentalDto();
        activeRentalDto.setId(4L);
        activeRentalDto.setRentalDate(LocalDate.of(2024, 2, 1));
        activeRentalDto.setReturnDate(LocalDate.of(2024, 2, 10));
        activeRentalDto.setActualReturnDate(LocalDate.of(2024, 2, 9));
        activeRentalDto.setCarId(2L);
        activeRentalDto.setUserId(6L);
        activeRentalDto.setIsActive("true");

        List<RentalDto> expected = new ArrayList<>();
        expected.add(activeRentalDto);

        MvcResult result = mockMvc.perform(get("/rentals")
                        .param("userId", userId.toString())
                        .param("isActive", Boolean.toString(isActive))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        RentalDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), RentalDto[].class);
        assertEquals(expected.size(), actual.length);
        assertEquals(expected, Arrays.stream(actual).toList());
    }

    @WithUserDetails("testuser@test.com")
    @Test
    @Sql(scripts = "classpath:database/users/add-test-users-cars-set.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/users/delete-test-users-cars-set.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Set rental actual return date is successful")
    void setRentalActualReturnDate_ValidRental_Success() throws Exception {
        Long rentalId = 1L;

        MvcResult result = mockMvc.perform(post("/rentals/" + rentalId + "/return")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        RentalDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), RentalDto.class);
        Car actualCar = carRepository.findById(actual.getCarId()).orElse(null);

        assertNotNull(actual);
        assertEquals(rentalId, actual.getId());
        assertNotNull(actual.getActualReturnDate());
        assertEquals(LocalDate.now(), actual.getActualReturnDate());
        assertEquals(11, actualCar.getInventory());
        assertEquals("false", actual.getIsActive());
    }

    @WithUserDetails("testuser@test.com")
    @Test
    @Sql(scripts = "classpath:database/users/add-test-users-cars-set.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/users/delete-test-users-cars-set.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Set rental actual return date for inactive rental is Bad Request")
    void setRentalActualReturnDate_InactiveRental_ReturnsBadRequest() throws Exception {
        Long rentalId = 3L;

        mockMvc.perform(post("/rentals/" + rentalId + "/return")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(scripts = {
            "classpath:database/users/delete-test-users-cars-set.sql",
            "classpath:database/users/add-test-users-cars-set.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/users/delete-test-users-cars-set.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Set actual return date for not owned active rental is Unauthorized")
    void setRentalActualReturnDate_ActiveRentalButNotOwned_ReturnsUnauthorized() throws Exception {
        Long rentalId = 4L;

        mockMvc.perform(post("/rentals/" + rentalId + "/return")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @WithUserDetails("testuser@test.com")
    @Test
    @Sql(scripts = "classpath:database/users/add-test-users-cars-set.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/users/delete-test-users-cars-set.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Set rental actual return date with future start date is Bad Request")
    void setRentalActualReturnDate_FutureRentalDate_ReturnsBadRequest() throws Exception {
        Long rentalId = 1L;
        Rental rental = rentalRepository.findById(rentalId).orElse(null);
        rental.setRentalDate(LocalDate.now().plusYears(5));
        rentalRepository.save(rental);

        mockMvc.perform(post("/rentals/" + rentalId + "/return")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @WithUserDetails("testuser@test.com")
    @Test
    @Sql(scripts = "classpath:database/users/add-test-users-cars-set.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/users/delete-test-users-cars-set.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Set rental actual return date with invalid rental Id returns Not Found")
    void setRentalActualReturnDate_InvalidRentalId_ReturnsNotFound() throws Exception {
        Long rentalId = 100L;

        mockMvc.perform(post("/rentals/" + rentalId + "/return")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
