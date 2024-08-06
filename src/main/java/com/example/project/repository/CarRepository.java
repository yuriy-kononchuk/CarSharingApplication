package com.example.project.repository;

import com.example.project.model.Car;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarRepository extends JpaRepository<Car, Long> {
    Optional<Car> findByBrandAndModelAndTypeAndDailyFee(
            String brand, String model, Car.CarType type, BigDecimal dailyFee);
}
