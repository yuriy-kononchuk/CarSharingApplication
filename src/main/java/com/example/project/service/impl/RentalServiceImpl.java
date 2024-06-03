package com.example.project.service.impl;

import com.example.project.dto.rental.CreateRentalRequestDto;
import com.example.project.dto.rental.RentalDto;
import com.example.project.exception.DataNotFoundException;
import com.example.project.exception.EntityNotFoundException;
import com.example.project.exception.IncorrectArgumentException;
import com.example.project.mapper.RentalMapper;
import com.example.project.model.Car;
import com.example.project.model.Payment;
import com.example.project.model.Rental;
import com.example.project.model.User;
import com.example.project.repository.CarRepository;
import com.example.project.repository.RentalRepository;
import com.example.project.service.RentalService;
import com.example.project.service.TelegramNotificationService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {
    private static final boolean ACTIVE = true;
    private static final int INCREASE_INVENTORY = 1;
    private static final int DECREASE_INVENTORY = -1;
    private static final BigDecimal FINE_MULTIPLIER = BigDecimal.valueOf(2.0);
    private static final boolean IS_ACTIVE = true;
    private final RentalRepository rentalRepository;
    private final RentalMapper rentalMapper;
    private final CarRepository carRepository;
    private final TelegramNotificationService telegramNotificationService;

    @Override
    public RentalDto save(User user, CreateRentalRequestDto requestDto) {
        if (requestDto.rentalDate().isBefore(LocalDate.now())) {
            throw new IncorrectArgumentException("Rental date must be today or later");
        }
        if (requestDto.returnDate().isBefore(requestDto.rentalDate())) {
            throw new IncorrectArgumentException("Return date must be as rental date or later");
        }
        Car carById = carRepository.findById(requestDto.carId()).orElseThrow(() ->
                new EntityNotFoundException("Can't find a car by id: " + requestDto.carId()));
        if (carById.getInventory() == 0) {
            throw new DataNotFoundException("Rental is not possible,"
                    + " there's no available car left. Try another car");
        }
        Rental rental = new Rental();
        rental.setRentalDate(requestDto.rentalDate());
        rental.setReturnDate(requestDto.returnDate());
        rental.setActualReturnDate(requestDto.returnDate());
        rental.setUser(user);
        rental.setCar(carById);
        rental.setActive(ACTIVE);

        updateInventoryCount(carById, DECREASE_INVENTORY);
        Rental savedRental = rentalRepository.save(rental);

        String message = String.format(
                "New Rental ID %s created: %s %s rented %s %s %s from %s to %s for %s "
                        + "USD daily fee",
                savedRental.getId(), user.getFirstName(), user.getLastName(), carById.getBrand(),
                carById.getModel(), carById.getType(), requestDto.rentalDate(),
                requestDto.returnDate(), carById.getDailyFee()
        );
        telegramNotificationService.sendMessage(message);

        return rentalMapper.toDto(savedRental);
    }

    @Override
    public RentalDto getById(Long userId, Long id) {
        List<Long> rentalIds = rentalRepository.findByUserId(userId).stream()
                .map(Rental::getId)
                .toList();
        if (!rentalIds.contains(id)) {
            throw new IncorrectArgumentException("There's no rental id " + id
                    + " in your rental's list");
        }
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Can't find a rental by id:" + id));
        return rentalMapper.toDto(rental);
    }

    @Override
    public List<RentalDto> getRentalsByUserIdAndIsActive(
            Long userId, boolean isActive, Pageable pageable) {
        List<Rental> rentalsByUserId = rentalRepository.findByUserId(userId);
        if (rentalsByUserId.isEmpty()) {
            throw new DataNotFoundException("There's no rentals by user ID: " + userId);
        }
        if (isActive) {
            rentalsByUserId = rentalRepository.findByUserIdAndIsActive(userId, true);

        } else {
            rentalsByUserId = rentalRepository.findByUserIdAndIsActive(userId, false);
        }
        return rentalsByUserId.stream()
                .map(rentalMapper::toDto)
                .toList();
    }

    @Override
    public RentalDto setRentalActualReturnDate(
            User user, Long rentalId) {
        Rental rentalById = rentalRepository.findByUserIdAndId(
                user.getId(), rentalId).orElseThrow(() -> new EntityNotFoundException(
                "Can't find a rental by id: " + rentalId));
        if (!rentalById.isActive()) {
            throw new IncorrectArgumentException("Can't set new actual return date, rental id: "
                    + rentalById.getId() + " is inactive. Try another rental");
        }
        if (LocalDate.now().isBefore(rentalById.getRentalDate())) {
            throw new IncorrectArgumentException("Can't set actual return date as today "
                    + "because this rental starts later. Rental id: "
                    + rentalById.getId());
        }
        rentalById.setActualReturnDate(LocalDate.now());
        rentalById.setActive(!ACTIVE); //conditions about payment needed??
        Car carById = carRepository.findById(rentalById.getCar().getId())
                .orElseThrow(() -> new EntityNotFoundException("Can't find a car by id: "
                        + rentalById.getCar().getId()));
        updateInventoryCount(carById, INCREASE_INVENTORY);
        return rentalMapper.toDto(rentalRepository.save(rentalById));
    }

    @Override
    public BigDecimal calculateRentalTotalPrice(Long rentalId, Payment.Type type) {
        Rental rentalById = rentalRepository.findById(rentalId).orElseThrow(()
                -> new EntityNotFoundException("Can't find a rental by id: " + rentalId));
        if (rentalById.isActive()) {
            throw new IncorrectArgumentException("Can't process. rental is still active. "
                    + "You need to complete this rental first, ID: " + rentalId);
        }
        BigDecimal dailyFee = rentalById.getCar().getDailyFee();
        long rentalDuration = ChronoUnit.DAYS.between(rentalById.getRentalDate(),
                rentalById.getActualReturnDate()) + INCREASE_INVENTORY;
        BigDecimal totalPrice = dailyFee.multiply(BigDecimal.valueOf(rentalDuration));
        if (type == Payment.Type.FINE
                && rentalById.getActualReturnDate().isAfter(rentalById.getRentalDate())) {
            long overdueDuration = ChronoUnit.DAYS.between(rentalById.getReturnDate(),
                    rentalById.getActualReturnDate());
            BigDecimal overdueMoneyToPay = dailyFee.multiply(BigDecimal.valueOf(overdueDuration))
                    .multiply(FINE_MULTIPLIER);
            totalPrice = totalPrice.add(overdueMoneyToPay);
        }
        return totalPrice;
    }

    @Override
    //@Scheduled(initialDelay = 90000, fixedDelay = 60000) // for testing
    @Scheduled(cron = "0 0 9 * * *") // Runs every day at 09:00 AM
    @Scheduled(cron = "0 0 21 * * *") // Runs every day at 09:00 PM
    public void getAllOverdueRentalsAndSendNotificationToUser() {
        List<Rental> overdueRentals = rentalRepository.findByIsActive(IS_ACTIVE).stream()
                .filter(rental -> LocalDate.now().isAfter(rental.getReturnDate()))
                .toList();
        if (!overdueRentals.isEmpty()) {
            overdueRentals.forEach(rental -> {
                User user = rental.getUser();
                Car car = rental.getCar();
                String days = "days";
                long overduePeriod = ChronoUnit.DAYS.between(
                        rental.getReturnDate(), LocalDate.now());
                if (overduePeriod == 1L) {
                    days = "day";
                }
                String message = String.format(
                        "Overdue Rental ID %s: User %s %s has an overdue rental for %s %s %s. "
                                + "Rental Date: %s, Expected Return Date: %s"
                                + System.lineSeparator()
                                + "The overdue period is " + overduePeriod + " " + days,
                        rental.getId(), user.getFirstName(), user.getLastName(),
                        car.getBrand(), car.getModel(), car.getType(),
                        rental.getRentalDate(), rental.getReturnDate()
                );
                telegramNotificationService.sendMessage(message);
            });
        }
        telegramNotificationService.sendMessage("No rentals overdue today!");
    }

    private void updateInventoryCount(Car car, int counterChange) {
        car.setInventory(car.getInventory() + counterChange);
        carRepository.save(car);
    }
}
