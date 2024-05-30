package com.example.project.controller;

import com.example.project.dto.car.CarDto;
import com.example.project.dto.car.CreateCarRequestDto;
import com.example.project.dto.car.UpdateCarRequestDto;
import com.example.project.service.CarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Car management", description = "Endpoints for mapping cars")
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/cars")
public class CarController {
    private final CarService carService;

    @ResponseStatus(HttpStatus.CREATED)
    //@PreAuthorize("hasRole('ROLE_MANAGER')")
    @PreAuthorize("hasAuthority('MANAGER')")
    @PostMapping
    @Operation(summary = "Create a new car", description = "Create a new car")
    @ApiResponse(responseCode = "201", description = "New car is successfully created")
    public CarDto createCar(@RequestBody @Valid CreateCarRequestDto carDto) {
        return carService.save(carDto);
    }

    @GetMapping
    @Operation(summary = "Get all Cars", description = "Get a list of all available cars")
    @ApiResponse(responseCode = "200", description = "Got a list of all available cars")
    public List<CarDto> getAllCars(Pageable pageable) {
        return carService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a car by id", description = "Get a car info by id")
    @ApiResponse(responseCode = "200", description = "Got a car's info by id")
    public CarDto getCarById(@PathVariable Long id) {
        return carService.getById(id);
    }

    @PreAuthorize("hasAuthority('MANAGER')")
    @PutMapping("/{id}")
    @Operation(summary = "Update car by id", description = "Update a car by id")
    @ApiResponse(responseCode = "200", description = "Requested car was updated")
    public CarDto updateCarById(@PathVariable Long id, @RequestBody UpdateCarRequestDto carDto) {
        return carService.updateById(id, carDto);
    }

    @PreAuthorize("hasAuthority('MANAGER')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete car by ID", description = "Delete a car by id")
    @ApiResponse(responseCode = "200", description = "Requested car was deleted with inventory decreased by 1")
    public void deleteCarById(@PathVariable Long id) {
        carService.deleteById(id);
    }
}
