package com.example.project.mapper;

import com.example.project.dto.car.CarDto;
import com.example.project.dto.car.CreateCarRequestDto;
import com.example.project.model.Car;
import org.springframework.stereotype.Component;

@Component
public class CarMapperImpl implements CarMapper {
    @Override
    public CarDto toDto(Car car) {
        if (car == null) {
            return null;
        }
        CarDto dto = new CarDto();
        dto.setId(car.getId());
        dto.setBrand(car.getBrand());
        dto.setModel(car.getModel());
        dto.setType(car.getType());
        dto.setInventory(car.getInventory());
        dto.setDailyFee(car.getDailyFee());
        return dto;
    }

    @Override
    public Car toEntity(CreateCarRequestDto requestDto) {
        if (requestDto == null) {
            return null;
        }
        Car car = new Car();
        car.setBrand(requestDto.brand());
        car.setModel(requestDto.model());
        car.setType(requestDto.type());
        car.setDailyFee(requestDto.dailyFee());
        return car;
    }
}
