package com.example.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.example.project.dto.car.CarDto;
import com.example.project.dto.car.CreateCarRequestDto;
import com.example.project.dto.car.UpdateCarRequestDto;
import com.example.project.exception.EntityNotFoundException;
import com.example.project.mapper.CarMapper;
import com.example.project.model.Car;
import com.example.project.repository.CarRepository;
import com.example.project.service.impl.CarServiceImpl;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {
    @Mock
    private CarRepository carRepository;
    @Mock
    private CarMapper carMapper;
    @InjectMocks
    private CarServiceImpl carService;

    @Test
    @DisplayName("Verify save() method creates a new car correctly")
    void save_NewCarAndValidCreateCarRequestDto_ReturnsCarDto() {
        CreateCarRequestDto requestDto = new CreateCarRequestDto("Chevrolet", "Equinox",
                Car.CarType.SUV, BigDecimal.valueOf(23.08));
        Car car = new Car();
        car.setBrand("Chevrolet");
        car.setModel("Equinox");
        car.setType(Car.CarType.SUV);
        car.setInventory(1);
        car.setDailyFee(BigDecimal.valueOf(23.08));
        CarDto expectedCarDto = new CarDto(1L, car.getBrand(), car.getModel(), car.getType(),
                car.getInventory(), car.getDailyFee());

        when(carRepository.findByBrandAndModelAndTypeAndDailyFee(
                requestDto.brand(), requestDto.model(), requestDto.type(), requestDto.dailyFee()))
                .thenReturn(Optional.empty());
        when(carMapper.toEntity(requestDto)).thenReturn(car);
        when(carRepository.save(car)).thenReturn(car);
        when(carMapper.toDto(car)).thenReturn(expectedCarDto);

        CarDto actualCarDto = carService.save(requestDto);

        verify(carRepository, times(1)).findByBrandAndModelAndTypeAndDailyFee(
                requestDto.brand(), requestDto.model(), requestDto.type(), requestDto.dailyFee());
        verify(carMapper, times(1)).toEntity(requestDto);
        verify(carRepository, times(1)).save(car);
        verify(carMapper, times(1)).toDto(car);
        verifyNoMoreInteractions(carRepository, carMapper);
        assertEquals(expectedCarDto, actualCarDto);
    }

    @Test
    @DisplayName("Verify save() method saves existing car and inventory correctly")
    void save_ExistingCarAndValidCreateCarRequestDto_ReturnsCarDto() {
        CreateCarRequestDto requestDto = new CreateCarRequestDto("Chevrolet", "Equinox",
                Car.CarType.SUV, BigDecimal.valueOf(23.08));
        Car existingCar = new Car();
        existingCar.setBrand("Chevrolet");
        existingCar.setModel("Equinox");
        existingCar.setType(Car.CarType.SUV);
        existingCar.setInventory(1);
        existingCar.setDailyFee(BigDecimal.valueOf(23.08));

        int increasedInventory = 2;
        CarDto expectedCarDto = new CarDto(1L, existingCar.getBrand(), existingCar.getModel(),
                existingCar.getType(), increasedInventory, existingCar.getDailyFee());

        when(carRepository.findByBrandAndModelAndTypeAndDailyFee(
                requestDto.brand(), requestDto.model(), requestDto.type(), requestDto.dailyFee()))
                .thenReturn(Optional.of(existingCar));
        when(carMapper.toDto(existingCar)).thenReturn(expectedCarDto);

        CarDto actualCarDto = carService.save(requestDto);

        verify(carRepository, times(1)).findByBrandAndModelAndTypeAndDailyFee(
                requestDto.brand(), requestDto.model(), requestDto.type(), requestDto.dailyFee());
        verify(carRepository, times(1)).save(existingCar);
        verify(carMapper, times(1)).toDto(existingCar);
        verifyNoMoreInteractions(carRepository, carMapper);
        assertEquals(expectedCarDto, actualCarDto);
        assertEquals(increasedInventory, existingCar.getInventory());
    }

    @Test
    @DisplayName("Verify findAll() method works correctly")
    void findAll_ValidPageable_ReturnsAllCarDtos() {
        Car car1 = new Car();
        car1.setBrand("Chevrolet");
        car1.setModel("Equinox");
        car1.setType(Car.CarType.SUV);
        car1.setInventory(3);
        car1.setDailyFee(BigDecimal.valueOf(23.08));

        Car car2 = new Car();
        car2.setBrand("Chevrolet");
        car2.setModel("Lacetti");
        car2.setType(Car.CarType.SEDAN);
        car2.setInventory(3);
        car2.setDailyFee(BigDecimal.valueOf(19.99));

        Car car3 = new Car();
        car3.setBrand("Toyota");
        car3.setModel("CH-R");
        car3.setType(Car.CarType.HATCHBACK);
        car3.setInventory(3);
        car3.setDailyFee(BigDecimal.valueOf(19.99));

        List<Car> cars = List.of(car1, car2, car3);

        CarDto carDto1 = new CarDto(1L, "Chevrolet", "Equinox", Car.CarType.SUV, 3,
                BigDecimal.valueOf(23.08));
        CarDto carDto2 = new CarDto(2L, "Chevrolet", "Lacetti", Car.CarType.SEDAN, 3,
                BigDecimal.valueOf(23.08));
        CarDto carDto3 = new CarDto(3L, "Toyota", "CH-R", Car.CarType.HATCHBACK, 3,
                BigDecimal.valueOf(23.08));
        List<CarDto> expectedCarDtos = List.of(carDto1, carDto2, carDto3);

        Pageable pageable = PageRequest.of(0, 10);

        when(carRepository.findAll()).thenReturn(cars);
        when(carMapper.toDto(car1)).thenReturn(carDto1);
        when(carMapper.toDto(car2)).thenReturn(carDto2);
        when(carMapper.toDto(car3)).thenReturn(carDto3);

        List<CarDto> actualCarDtos = carService.findAll(pageable);

        AssertionsForClassTypes.assertThat(actualCarDtos.size()).isEqualTo(3);
        AssertionsForClassTypes.assertThat(actualCarDtos.get(0)).isEqualTo(carDto1);
        AssertionsForClassTypes.assertThat(actualCarDtos.get(1)).isEqualTo(carDto2);
        AssertionsForClassTypes.assertThat(actualCarDtos.get(2)).isEqualTo(carDto3);
        assertEquals(expectedCarDtos, actualCarDtos);
        verify(carRepository, times(1)).findAll();
        verify(carMapper, times(1)).toDto(car1);
        verify(carMapper, times(1)).toDto(car2);
        verify(carMapper, times(1)).toDto(car3);
        verifyNoMoreInteractions(carRepository, carMapper);
    }

    @Test
    @DisplayName("Verify getById() method works correctly")
    void getById_ValidCarId_ReturnsValidCarDto() {
        Car car = new Car();
        car.setBrand("Chevrolet");
        car.setModel("Equinox");
        car.setType(Car.CarType.SUV);
        car.setInventory(3);
        car.setDailyFee(BigDecimal.valueOf(23.08));

        Long carId = 1L;

        CarDto expectedCarDto = new CarDto(carId, "Chevrolet", "Equinox", Car.CarType.SUV, 3,
                BigDecimal.valueOf(23.08));

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(carMapper.toDto(car)).thenReturn(expectedCarDto);

        CarDto actualCarDto = carService.getById(carId);

        AssertionsForClassTypes.assertThat(actualCarDto).isEqualTo(expectedCarDto);
        verify(carRepository, times(1)).findById(carId);
        verify(carMapper, times(1)).toDto(car);
        verifyNoMoreInteractions(carRepository, carMapper);
    }

    @Test
    @DisplayName("Verify getById() method with wrong Id throws exception")
    void getById_WithNonValidCarId_ThrowsException() {
        Long carId = 100L;

        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> carService.getById(carId));

        String expected = "Can't find a car by id: " + carId;
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify updateById() method updates and returns CarDto with valid id")
    void updateById_ValidId_UpdatesAndReturnsCarDto() {
        Long carId = 1L;
        int updatedInventory = 5;
        BigDecimal updatedDailyFee = BigDecimal.valueOf(29.99);
        UpdateCarRequestDto requestDto = new UpdateCarRequestDto(updatedInventory, updatedDailyFee);

        Car existingCar = new Car();
        existingCar.setId(carId);
        existingCar.setBrand("Chevrolet");
        existingCar.setModel("Equinox");
        existingCar.setType(Car.CarType.SUV);
        existingCar.setInventory(3);
        existingCar.setDailyFee(BigDecimal.valueOf(23.08));

        Car updatedCar = new Car();
        updatedCar.setId(carId);
        updatedCar.setBrand("Chevrolet");
        updatedCar.setModel("Equinox");
        updatedCar.setType(Car.CarType.SUV);
        updatedCar.setDailyFee(requestDto.dailyFee());
        updatedCar.setInventory(requestDto.inventory());

        CarDto expectedCarDto = new CarDto(carId, "Chevrolet", "Equinox", Car.CarType.SUV,
                updatedInventory, updatedDailyFee);

        when(carRepository.findById(carId)).thenReturn(Optional.of(existingCar));
        when(carRepository.save(any(Car.class))).thenReturn(updatedCar);
        when(carMapper.toDto(updatedCar)).thenReturn(expectedCarDto);

        CarDto actualCarDto = carService.updateById(carId, requestDto);

        AssertionsForClassTypes.assertThat(actualCarDto).isEqualTo(expectedCarDto);
        verify(carRepository, times(1)).findById(carId);
        verify(carRepository, times(1)).save(any(Car.class));
        verify(carMapper, times(1)).toDto(updatedCar);
        verifyNoMoreInteractions(carRepository, carMapper);
    }

    @Test
    @DisplayName("Verify updateById() method with wrong Id throws exception")
    void updateById_WithNonValidCarId_ThrowsException() {
        Long carId = 100L;
        UpdateCarRequestDto requestDto = new UpdateCarRequestDto(5, BigDecimal.valueOf(9.99));

        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> carService.updateById(carId, requestDto));

        String expected = "Can't find a car by id: " + carId;
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify deleteById() method deletes car with valid id")
    void deleteById_ValidCarIdAndInventoryIsOne_DeletesCar() {
        Long carId = 1L;
        int inventory = 1;

        Car existingCar = new Car();
        existingCar.setId(carId);
        existingCar.setBrand("Chevrolet");
        existingCar.setModel("Equinox");
        existingCar.setType(Car.CarType.SUV);
        existingCar.setInventory(inventory);

        when(carRepository.findById(carId)).thenReturn(Optional.of(existingCar));

        carService.deleteById(carId);
        int actualInventory = existingCar.getInventory();

        assertEquals(actualInventory, 0);
        verify(carRepository, times(1)).findById(carId);
        verify(carRepository, times(1)).save(existingCar);
        verify(carRepository, times(1)).delete(existingCar);
        verifyNoMoreInteractions(carRepository);
    }

    @Test
    @DisplayName("Verify deleteById() method decreases inventory by one and with valid id")
    void deleteById_ValidCarIdAndInventoryMoreThanOne_DecreasesCarInventoryByOne() {
        Long carId = 1L;
        int inventory = 3;

        Car existingCar = new Car();
        existingCar.setId(carId);
        existingCar.setBrand("Chevrolet");
        existingCar.setModel("Equinox");
        existingCar.setType(Car.CarType.SUV);
        existingCar.setInventory(inventory);

        int expectedInventory = existingCar.getInventory() - 1;

        when(carRepository.findById(carId)).thenReturn(Optional.of(existingCar));

        carService.deleteById(carId);
        int actualInventory = existingCar.getInventory();

        assertEquals(expectedInventory, actualInventory);
        verify(carRepository, times(1)).findById(carId);
        verify(carRepository, times(1)).save(existingCar);
        verifyNoMoreInteractions(carRepository);
    }

    @Test
    @DisplayName("Verify deleteById() method with wrong Id throws exception")
    void deleteById_WithNonValidCarId_ThrowsException() {
        Long carId = 100L;

        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> carService.deleteById(carId));

        String expected = "There's no car to delete by id: " + carId;
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }
}
