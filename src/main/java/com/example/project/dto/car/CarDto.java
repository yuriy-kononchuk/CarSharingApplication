package com.example.project.dto.car;

import com.example.project.model.Car;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class CarDto {
    private Long id;
    private String brand;
    private String model;
    private Car.CarType type;
    private int inventory;
    private BigDecimal dailyFee;
}
