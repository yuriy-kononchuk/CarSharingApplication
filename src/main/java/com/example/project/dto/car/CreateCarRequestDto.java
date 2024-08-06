package com.example.project.dto.car;

import com.example.project.model.Car;
import java.math.BigDecimal;

public record CreateCarRequestDto(
        String brand,
        String model,
        Car.CarType type,
        BigDecimal dailyFee
) {
}
