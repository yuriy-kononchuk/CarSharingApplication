package com.example.project.service.impl;

import com.example.project.dto.car.CarDto;
import com.example.project.dto.car.CreateCarRequestDto;
import com.example.project.dto.car.UpdateCarRequestDto;
import com.example.project.exception.EntityNotFoundException;
import com.example.project.mapper.CarMapper;
import com.example.project.model.Car;
import com.example.project.repository.CarRepository;
import com.example.project.service.CarService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {
    private static final int INCREASE_INVENTORY = 1;
    private static final int DECREASE_INVENTORY = -1;
    private final CarRepository carRepository;
    private final CarMapper carMapper;

    @Override
    public List<CarDto> findAll(Pageable pageable) {
        return carRepository.findAll().stream()
                .map(carMapper::toDto)
                .toList();
    }

    @Override
    public CarDto getById(Long id) {
        Car car = carRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(
                "Can't find a car by id: " + id));
        return carMapper.toDto(car);
    }

    @Override
    public CarDto save(CreateCarRequestDto requestDto) {
        Optional<Car> existingCarOptional = carRepository.findByBrandAndModelAndTypeAndDailyFee(
                requestDto.brand(), requestDto.model(), requestDto.type(), requestDto.dailyFee());
        if (existingCarOptional.isEmpty()) {
            Car addedCar = carMapper.toEntity(requestDto);
            addedCar.setInventory(INCREASE_INVENTORY);
            return carMapper.toDto(carRepository.save(addedCar));
        }
        Car existingCar = existingCarOptional.get();
        updateInventoryCount(existingCar, INCREASE_INVENTORY);
        return carMapper.toDto(existingCar);
    }

    @Override
    public CarDto updateById(Long id, UpdateCarRequestDto requestDto) {
        Car carToUpdate = carRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(
                "Can't find a car by id: " + id));
        carToUpdate.setInventory(requestDto.inventory());
        carToUpdate.setDailyFee(requestDto.dailyFee());
        return carMapper.toDto(carRepository.save(carToUpdate));
    }

    @Override
    public void deleteById(Long id) {
        Car carToDelete = carRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(
                "There's no car to delete by id: " + id));
        updateInventoryCount(carToDelete, DECREASE_INVENTORY);
    }

    private void updateInventoryCount(Car car, int counterChange) {
        car.setInventory(car.getInventory() + counterChange);
        if (car.getInventory() == 0) {
            carRepository.delete(car);
        } else {
            carRepository.save(car);
        }
    }
}
