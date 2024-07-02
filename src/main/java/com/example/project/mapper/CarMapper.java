package com.example.project.mapper;

import com.example.project.config.MapperConfig;
import com.example.project.dto.car.CarDto;
import com.example.project.dto.car.CreateCarRequestDto;
import com.example.project.model.Car;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface CarMapper {
    CarDto toDto(Car car);

    Car toEntity(CreateCarRequestDto requestDto);

}
