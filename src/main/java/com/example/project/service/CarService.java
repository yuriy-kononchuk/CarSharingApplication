package com.example.project.service;

import com.example.project.dto.car.CarDto;
import com.example.project.dto.car.CreateCarRequestDto;
import com.example.project.dto.car.UpdateCarRequestDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface CarService {
    CarDto save(CreateCarRequestDto requestDto);

    List<CarDto> findAll(Pageable pageable);

    CarDto getById(Long id);

    CarDto updateById(Long id, UpdateCarRequestDto requestDto);

    void deleteById(Long id);
}
