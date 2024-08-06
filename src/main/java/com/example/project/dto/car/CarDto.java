package com.example.project.dto.car;

import com.example.project.model.Car;
import java.math.BigDecimal;

public record CarDto(
        Long id,
        String brand,
        String model,
        Car.CarType type,
        int inventory,
        BigDecimal dailyFee
) {
}
