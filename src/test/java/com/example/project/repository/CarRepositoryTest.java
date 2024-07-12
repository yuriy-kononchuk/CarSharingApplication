package com.example.project.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.project.model.Car;
import java.math.BigDecimal;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "classpath:database/cars/add-test-cars-to-cars-table.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/cars/delete-test-cars-from-cars-table.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class CarRepositoryTest {
    @Autowired
    private CarRepository carRepository;

    @Test
    @DisplayName("Find car by given brand, model, type, and daily fee")
    void findByBrandAndModelAndTypeAndDailyFee_ValidDataToFind_ReturnsCarOptional() {
        String brand = "Chevrolet";
        String model = "Equinox";
        Car.CarType type = Car.CarType.SUV;
        BigDecimal dailyFee = BigDecimal.valueOf(50.00);

        Optional<Car> foundCar = carRepository.findByBrandAndModelAndTypeAndDailyFee(
                brand, model, type, dailyFee);

        Assertions.assertThat(foundCar).isPresent();
        assertEquals(1L, foundCar.get().getId());
        assertEquals("Chevrolet", foundCar.get().getBrand());
        assertEquals("Equinox", foundCar.get().getModel());
        assertEquals(Car.CarType.SUV, foundCar.get().getType());
        assertEquals(10, foundCar.get().getInventory());
        assertEquals(0, foundCar.get().getDailyFee().compareTo(dailyFee));
    }

    @Test
    @DisplayName("Verify no car found with wrong model and type")
    void findByBrandAndModelAndTypeAndDailyFee_InvalidModelType_ReturnsEmptyOptional() {
        String brand = "Chevrolet";
        String model = "Lacetti";
        Car.CarType type = Car.CarType.SEDAN;
        BigDecimal dailyFee = BigDecimal.valueOf(50.00);

        Optional<Car> foundCar = carRepository.findByBrandAndModelAndTypeAndDailyFee(
                brand, model, type, dailyFee);

        Assertions.assertThat(foundCar).isEmpty();
    }

    @Test
    @DisplayName("Verify no car found with wrong brand")
    void findByBrandAndModelAndTypeAndDailyFee_InvalidBrand_ReturnsEmptyOptional() {
        String brand = "FORD";
        String model = "Equinox";
        Car.CarType type = Car.CarType.SUV;
        BigDecimal dailyFee = BigDecimal.valueOf(50.00);

        Optional<Car> foundCar = carRepository.findByBrandAndModelAndTypeAndDailyFee(
                brand, model, type, dailyFee);

        Assertions.assertThat(foundCar).isEmpty();
    }
}
