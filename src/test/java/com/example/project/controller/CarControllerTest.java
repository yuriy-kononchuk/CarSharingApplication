package com.example.project.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.project.dto.car.CarDto;
import com.example.project.dto.car.CreateCarRequestDto;
import com.example.project.dto.car.UpdateCarRequestDto;
import com.example.project.model.Car;
import com.example.project.repository.CarRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class CarControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CarRepository carRepository;

    @WithMockUser(username = "admin", authorities = {"MANAGER"})
    @Test
    @Sql(scripts = "classpath:database/cars/delete-test-cars-from-cars-table.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Create a new car by 'MANAGER' is successful")
    void createCar_ValidRequestDto_Success() throws Exception {
        CreateCarRequestDto requestDto = new CreateCarRequestDto(
                "Chevrolet", "Equinox", Car.CarType.SUV, BigDecimal.valueOf(23.08));
        CarDto expectedDto = new CarDto(1L, requestDto.brand(), requestDto.model(),
                requestDto.type(), 1, requestDto.dailyFee());

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(post("/cars")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        CarDto actualDto = objectMapper.readValue(result.getResponse().getContentAsString(),
                CarDto.class);

        assertNotNull(actualDto);
        assertNotNull(actualDto.id());
        assertEquals(expectedDto.brand(), actualDto.brand());
        assertEquals(expectedDto.model(), actualDto.model());
        assertEquals(expectedDto.type(), actualDto.type());
        assertEquals(expectedDto.dailyFee(), actualDto.dailyFee());
    }

    @WithMockUser(username = "user", authorities = {"CUSTOMER"})
    @Test
    @DisplayName("Create a new car by 'CUSTOMER' is Forbidden")
    void createCar_UserWithoutManagerAuthority_Forbidden() throws Exception {
        CreateCarRequestDto requestDto = new CreateCarRequestDto(
                "Chevrolet", "Equinox", Car.CarType.SUV, BigDecimal.valueOf(23.08));

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(post("/cars")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(username = "any_user", authorities = {"CUSTOMER"})
    @Test
    @Sql(scripts = "classpath:database/cars/add-test-cars-to-cars-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/cars/delete-test-cars-from-cars-table.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Get all cars is successful")
    void getAllCars_ReturnsListAllCarDtos_Success() throws Exception {
        List<CarDto> expected = new ArrayList<>();
        expected.add(new CarDto(
                1L, "Chevrolet", "Equinox", Car.CarType.SUV, 10, BigDecimal.valueOf(50.00)));
        expected.add(new CarDto(
                2L, "Ford", "Focus", Car.CarType.HATCHBACK, 15, BigDecimal.valueOf(45.00)));
        expected.add(new CarDto(
                3L, "Honda", "Civic", Car.CarType.SEDAN, 5, BigDecimal.valueOf(55.00)));
        expected.add(new CarDto(
                4L, "Chevrolet", "Equinox", Car.CarType.SUV, 5, BigDecimal.valueOf(23.08)));

        MvcResult result = mockMvc.perform(get("/cars")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        CarDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), CarDto[].class);
        assertEquals(expected.size(), actual.length);

        List<CarDto> actualList = Arrays.asList(actual);
        expected.forEach(expectedCar -> {
            CarDto actualCar = actualList.stream()
                    .filter(car -> car.id().equals(expectedCar.id()))
                    .findFirst()
                    .orElse(null);

            assertNotNull(actualCar);
            assertEquals(expectedCar.brand(), actualCar.brand());
            assertEquals(expectedCar.model(), actualCar.model());
            assertEquals(expectedCar.type(), actualCar.type());
            assertEquals(expectedCar.inventory(), actualCar.inventory());
            assertEquals(0, expectedCar.dailyFee().compareTo(actualCar.dailyFee()));
        });
    }

    @WithMockUser(username = "any_user", authorities = {"CUSTOMER"})
    @Test
    @Sql(scripts = "classpath:database/cars/add-test-cars-to-cars-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/cars/delete-test-cars-from-cars-table.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Get car by valid Id is successful")
    void getCarById_ValidId_ReturnsCarDto() throws Exception {
        Long carId = 4L;

        MvcResult result = mockMvc.perform(get("/cars/" + carId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        CarDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                CarDto.class);

        assertNotNull(actual);
        assertNotNull(actual.id());
        assertEquals("Chevrolet", actual.brand());
        assertEquals("Equinox", actual.model());
        assertEquals(Car.CarType.SUV, actual.type());
        assertEquals(5, actual.inventory());
        assertEquals(0, BigDecimal.valueOf(23.08).compareTo(actual.dailyFee()));
    }

    @WithMockUser(username = "any_user", authorities = {"CUSTOMER"})
    @Test
    @DisplayName("Get car by invalid Id returns Not Found")
    void getCarById_InvalidId_ReturnsNotFound() throws Exception {
        Long invalidCarId = 100L;

        mockMvc.perform(get("/cars/" + invalidCarId))
                .andExpect(status().isNotFound());
    }

    @WithMockUser(username = "admin", authorities = {"MANAGER"})
    @Test
    @Sql(scripts = "classpath:database/cars/add-test-cars-to-cars-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/cars/delete-test-cars-from-cars-table.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Update car by valid Id is successful")
    void updateCarById_ValidIdAndRequestDto_ReturnsUpdatedCarDto() throws Exception {
        Long carId = 3L;
        UpdateCarRequestDto updateDto = new UpdateCarRequestDto(100, BigDecimal.valueOf(100.00));

        String jsonRequest = objectMapper.writeValueAsString(updateDto);

        MvcResult result = mockMvc.perform(put("/cars/" + carId)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        CarDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), CarDto.class);

        assertNotNull(actual);
        assertEquals(carId, actual.id());
        assertEquals(updateDto.inventory(), actual.inventory());
        assertEquals(0, updateDto.dailyFee().compareTo(actual.dailyFee()));
    }

    @WithMockUser(username = "user", authorities = {"CUSTOMER"})
    @Test
    @DisplayName("Update car by 'CUSTOMER' is Forbidden")
    void updateCarById_UserWithoutManagerAuthority_Forbidden() throws Exception {
        Long carId = 3L;
        UpdateCarRequestDto updateDto = new UpdateCarRequestDto(100, BigDecimal.valueOf(100.00));

        String jsonRequest = objectMapper.writeValueAsString(updateDto);

        mockMvc.perform(put("/cars/" + carId)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(username = "admin", authorities = {"MANAGER"})
    @Test
    @DisplayName("Update car by invalid Id returns Not Found")
    void updateCarById_InvalidId_ReturnsNotFound() throws Exception {
        Long invalidCarId = 100L;
        UpdateCarRequestDto updateDto = new UpdateCarRequestDto(100, BigDecimal.valueOf(100.00));

        String jsonRequest = objectMapper.writeValueAsString(updateDto);

        mockMvc.perform(put("/cars/" + invalidCarId)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @WithMockUser(username = "admin", authorities = {"MANAGER"})
    @Test
    @Sql(scripts = "classpath:database/cars/add-test-cars-to-cars-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/cars/delete-test-cars-from-cars-table.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Delete car by valid Id and decrease inventory is successful")
    void deleteCarById_ValidId_Success() throws Exception {
        Long carOneId = 1L;
        Long carTwoId = 2L;

        mockMvc.perform(delete("/cars/" + carOneId))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/cars/" + carTwoId))
                .andExpect(status().isOk());

        Car actualOne = carRepository.findById(carOneId).orElse(null);
        Car actualTwo = carRepository.findById(carTwoId).orElse(null);

        assertNotNull(actualOne);
        assertNotNull(actualTwo);
        assertEquals(9, actualOne.getInventory());
        assertEquals(14, actualTwo.getInventory());
    }

    @WithMockUser(username = "user", authorities = {"CUSTOMER"})
    @Test
    @DisplayName("Delete car by 'CUSTOMER' is Forbidden")
    void deleteCarById_UserWithoutManagerAuthority_Forbidden() throws Exception {
        Long carId = 1L;

        mockMvc.perform(delete("/cars/" + carId))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(username = "admin", authorities = {"MANAGER"})
    @Test
    @DisplayName("Delete car by invalid Id returns Not Found")
    void deleteCarById_InvalidId_ReturnsNotFound() throws Exception {
        Long invalidCarId = 100L;

        mockMvc.perform(delete("/cars/" + invalidCarId))
                .andExpect(status().isNotFound());
    }
}
