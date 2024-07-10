package com.example.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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
import com.example.project.service.impl.RentalServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
class RentalServiceTest {
    @Mock
    private CarRepository carRepository;
    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private RentalMapper rentalMapper;
    @Mock
    private TelegramNotificationService telegramNotificationService;
    @InjectMocks
    private RentalServiceImpl rentalService;

    @Test
    @DisplayName("Verify save() method creates RentalDto correctly with valid data")
    void save_ValidCreateRentalRequestDto_ReturnsRentalDto() {
        Long carId = 1L;
        Long rentalId = 1L;

        User user = new User();
        user.setId(1L);
        user.setFirstName("User");
        user.setLastName("First");

        CreateRentalRequestDto requestDto = new CreateRentalRequestDto(carId,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));

        Car car = new Car();
        car.setId(carId);
        car.setBrand("Chevrolet");
        car.setModel("Equinox");
        car.setType(Car.CarType.SUV);
        car.setInventory(3);
        car.setDailyFee(BigDecimal.valueOf(23.08));

        int expectedCarInventory = car.getInventory() - 1;

        Rental rental = Rental.builder()
                .id(rentalId)
                .rentalDate(requestDto.rentalDate())
                .returnDate(requestDto.returnDate())
                .actualReturnDate(requestDto.returnDate())
                .user(user)
                .car(car)
                .isActive(true)
                .build();

        RentalDto expectedRentalDto = new RentalDto();
        expectedRentalDto.setId(rentalId);
        expectedRentalDto.setRentalDate(rental.getRentalDate());
        expectedRentalDto.setReturnDate(rental.getReturnDate());
        expectedRentalDto.setActualReturnDate(rental.getActualReturnDate());
        expectedRentalDto.setCarId(carId);
        expectedRentalDto.setUserId(user.getId());
        expectedRentalDto.setIsActive("ACTIVE");

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(rentalRepository.save(any(Rental.class))).thenReturn(rental);
        when(rentalMapper.toDto(rental)).thenReturn(expectedRentalDto);

        RentalDto actualRentalDto = rentalService.save(user, requestDto);
        int actualCarInventory = car.getInventory();

        AssertionsForClassTypes.assertThat(actualRentalDto).isEqualTo(expectedRentalDto);
        assertEquals(expectedCarInventory, actualCarInventory);
        verify(carRepository, times(1)).findById(carId);
        verify(carRepository, times(1)).save(car);
        verify(rentalRepository, times(1)).save(any(Rental.class));
        verify(rentalMapper, times(1)).toDto(rental);
        verify(telegramNotificationService, times(1)).sendMessage(anyString());
        verifyNoMoreInteractions(carRepository, rentalRepository, rentalMapper, telegramNotificationService);
    }

    @Test
    @DisplayName("Verify save() method with wrong car id throws exception")
    void save_InvalidCarId_ThrowsException() {
        Long carId = 100L;

        User user = new User();
        user.setId(1L);

        CreateRentalRequestDto requestDto = new CreateRentalRequestDto(carId,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));

        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> rentalService.save(user, requestDto));

        assertEquals("Can't find a car by id: " + carId, exception.getMessage());

        verify(carRepository, times(1)).findById(carId);
        verifyNoMoreInteractions(
                carRepository, rentalRepository, rentalMapper, telegramNotificationService);
    }

    @Test
    @DisplayName("Verify save() method throws Exception when car inventory is zero")
    void save_NoAvailableCars_ThrowsException() {
        Long carId = 1L;

        User user = new User();
        user.setId(1L);

        CreateRentalRequestDto requestDto = new CreateRentalRequestDto(carId,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));

        Car car = new Car();
        car.setId(carId);
        car.setInventory(0);

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));

        Exception exception = assertThrows(DataNotFoundException.class,
                () -> rentalService.save(user, requestDto));

        assertEquals("Rental is not possible, there's no available car left. Try another car",
                exception.getMessage());

        verify(carRepository, times(1)).findById(carId);
        verifyNoMoreInteractions(carRepository, rentalRepository, rentalMapper,
                telegramNotificationService);
    }

    @Test
    @DisplayName("Verify save() method throws Exception for rental date in the past")
    void save_RentalDateInPast_ThrowstException() {
        Long carId = 1L;

        User user = new User();
        user.setId(1L);

        CreateRentalRequestDto requestDto = new CreateRentalRequestDto(carId,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(5));

        Exception exception = assertThrows(IncorrectArgumentException.class,
                () -> rentalService.save(user, requestDto));

        assertEquals("Rental date must be today or later", exception.getMessage());

        verifyNoInteractions(carRepository, rentalRepository, rentalMapper,
                telegramNotificationService);
    }

    @Test
    @DisplayName("Verify save() method throws Exception if return date is before rental date")
    void save_ReturnDateIsBeforeRentalDate_Exception() {
        Long carId = 1L;

        User user = new User();
        user.setId(1L);

        CreateRentalRequestDto requestDto = new CreateRentalRequestDto(carId,
                LocalDate.now().plusDays(5), LocalDate.now().plusDays(4));

        Exception exception = assertThrows(IncorrectArgumentException.class,
                () -> rentalService.save(user, requestDto));

        assertEquals("Return date must be as rental date or later", exception.getMessage());

        verifyNoInteractions(carRepository, rentalRepository, rentalMapper,
                telegramNotificationService);
    }

    @Test
    @DisplayName("Verify getById() method returns RentalDtos list for valid user and rental Ids")
    void getById_ValidUserIdAndRentalId_ReturnsRentalDto() {
        Long userId = 1L;
        Long rentalId = 1L;

        User user = new User();
        user.setId(userId);
        user.setFirstName("User");
        user.setLastName("First");

        Car car = new Car();
        car.setId(1L);
        car.setBrand("Chevrolet");
        car.setModel("Equinox");
        car.setType(Car.CarType.SUV);
        car.setInventory(3);
        car.setDailyFee(BigDecimal.valueOf(23.08));

        Rental rental = Rental.builder()
                .id(rentalId)
                .user(user)
                .car(car)
                .build();

        List<Rental> userRentals = List.of(rental);

        RentalDto expectedRentalDto = new RentalDto();
        expectedRentalDto.setId(rentalId);
        expectedRentalDto.setUserId(userId);
        expectedRentalDto.setCarId(car.getId());

        when(rentalRepository.findByUserId(userId)).thenReturn(userRentals);
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        when(rentalMapper.toDto(rental)).thenReturn(expectedRentalDto);

        RentalDto actualRentalDto = rentalService.getById(userId, rentalId);

        AssertionsForClassTypes.assertThat(actualRentalDto).isEqualTo(expectedRentalDto);
        verify(rentalRepository, times(1)).findByUserId(userId);
        verify(rentalRepository, times(1)).findById(rentalId);
        verify(rentalMapper, times(1)).toDto(rental);
        verifyNoMoreInteractions(rentalRepository, rentalMapper);
    }

    @Test
    @DisplayName("Verify getById() method throws Exception for invalid rentalId")
    void getById_InvalidRentalId_ThrowsException() {
        Long userId = 1L;
        Long rentalId = 1L;

        Rental rental = new Rental();
        rental.setId(5L);

        List<Rental> userRentals = List.of(rental);

        when(rentalRepository.findByUserId(userId)).thenReturn(userRentals);

        Exception exception = assertThrows(IncorrectArgumentException.class,
                () -> rentalService.getById(userId, rentalId));

        assertEquals("There's no rental id " + rentalId + " in your rental's list",
                exception.getMessage());

        verify(rentalRepository, times(1)).findByUserId(userId);
        verifyNoMoreInteractions(rentalRepository);
    }

    @Test
    @DisplayName("Verify getById() method throws Exception if rental is not found")
    void getById_RentalNotFound_Exception() {
        Long userId = 1L;
        Long rentalId = 1L;

        Rental rental = new Rental();
        rental.setId(rentalId);

        List<Rental> userRentals = List.of(rental);

        when(rentalRepository.findByUserId(userId)).thenReturn(userRentals);
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> rentalService.getById(userId, rentalId));

        assertEquals("Can't find a rental by id:" + rentalId, exception.getMessage());

        verify(rentalRepository, times(1)).findByUserId(userId);
        verify(rentalRepository, times(1)).findById(rentalId);
        verifyNoMoreInteractions(rentalRepository);
    }

    @Test
    @DisplayName("Verify getRentalsByUserIdAndIsActive() method returns RentalDto list "
            + "of active rentals for valid userId")
    void getRentalsByUserIdAndIsActive_ValidUserIdAndActive_ReturnsActiveRentalDtos() {
        Long userId = 1L;
        boolean isActive = true;
        Pageable pageable = PageRequest.of(0, 10);

        Rental rental1 = Rental.builder()
                .id(1L)
                .isActive(true)
                .build();
        Rental rental2 = Rental.builder()
                .id(2L)
                .isActive(true)
                .build();

        List<Rental> activeRentals = List.of(rental1, rental2);

        RentalDto rentalDto1 = new RentalDto();
        rentalDto1.setId(rental1.getId());
        RentalDto rentalDto2 = new RentalDto();
        rentalDto2.setId(rental2.getId());

        List<RentalDto> expectedRentalDtos = List.of(rentalDto1, rentalDto2);

        when(rentalRepository.findByUserId(userId)).thenReturn(activeRentals);
        when(rentalRepository.findByUserIdAndIsActive(userId, isActive)).thenReturn(activeRentals);
        when(rentalMapper.toDto(rental1)).thenReturn(rentalDto1);
        when(rentalMapper.toDto(rental2)).thenReturn(rentalDto2);

        List<RentalDto> actualRentalDtos = rentalService.getRentalsByUserIdAndIsActive(
                userId, isActive, pageable);

        AssertionsForClassTypes.assertThat(actualRentalDtos).isEqualTo(expectedRentalDtos);

        verify(rentalRepository, times(1)).findByUserId(userId);
        verify(rentalRepository, times(1)).findByUserIdAndIsActive(userId, isActive);
        verify(rentalMapper, times(1)).toDto(rental1);
        verify(rentalMapper, times(1)).toDto(rental2);
        verifyNoMoreInteractions(rentalRepository, rentalMapper);
    }

    @Test
    @DisplayName("Verify getRentalsByUserIdAndIsActive() method returns RentalDto list "
            + "of inactive rentals for valid userId")
    void getRentalsByUserIdAndIsNotActive_ValidUserIdAndActive_ReturnsInactiveRentalDtos() {
        Long userId = 1L;
        boolean isActive = false;
        Pageable pageable = PageRequest.of(0, 10);

        Rental rental1 = Rental.builder()
                .id(1L)
                .isActive(false)
                .build();
        Rental rental2 = Rental.builder()
                .id(2L)
                .isActive(false)
                .build();

        List<Rental> activeRentals = List.of(rental1, rental2);

        RentalDto rentalDto1 = new RentalDto();
        rentalDto1.setId(rental1.getId());
        RentalDto rentalDto2 = new RentalDto();
        rentalDto2.setId(rental2.getId());

        List<RentalDto> expectedRentalDtos = List.of(rentalDto1, rentalDto2);

        when(rentalRepository.findByUserId(userId)).thenReturn(activeRentals);
        when(rentalRepository.findByUserIdAndIsActive(userId, isActive)).thenReturn(activeRentals);
        when(rentalMapper.toDto(rental1)).thenReturn(rentalDto1);
        when(rentalMapper.toDto(rental2)).thenReturn(rentalDto2);

        List<RentalDto> actualRentalDtos = rentalService.getRentalsByUserIdAndIsActive(
                userId, isActive, pageable);

        AssertionsForClassTypes.assertThat(actualRentalDtos).isEqualTo(expectedRentalDtos);

        verify(rentalRepository, times(1)).findByUserId(userId);
        verify(rentalRepository, times(1)).findByUserIdAndIsActive(userId, isActive);
        verify(rentalMapper, times(1)).toDto(rental1);
        verify(rentalMapper, times(1)).toDto(rental2);
        verifyNoMoreInteractions(rentalRepository, rentalMapper);
    }

    @Test
    @DisplayName("Verify getRentalsByUserIdAndIsActive() method returns empty RentalDto list "
            + "for valid userId")
    void getRentalsByUserIdAndIsActiveAndRentalsotActive_ValidUserIdAndActive_ReturnsEmpptyList() {
        Long userId = 1L;
        boolean isActive = true;
        Pageable pageable = PageRequest.of(0, 10);

        Rental rental1 = Rental.builder()
                .id(1L)
                .isActive(false)
                .build();
        Rental rental2 = Rental.builder()
                .id(2L)
                .isActive(false)
                .build();

        List<Rental> rentals = List.of(rental1, rental2);

        when(rentalRepository.findByUserId(userId)).thenReturn(rentals);
        when(rentalRepository.findByUserIdAndIsActive(userId, isActive))
                .thenReturn(Collections.emptyList());

        List<RentalDto> expectedRentalDtos = Collections.emptyList();

        List<RentalDto> actualRentalDtos = rentalService.getRentalsByUserIdAndIsActive(userId, isActive, pageable);

        AssertionsForClassTypes.assertThat(actualRentalDtos).isEqualTo(expectedRentalDtos);

        verify(rentalRepository, times(1)).findByUserId(userId);
        verify(rentalRepository, times(1)).findByUserIdAndIsActive(userId, isActive);
        verifyNoInteractions(rentalMapper);
        verifyNoMoreInteractions(rentalRepository);
    }

    @Test
    @DisplayName("Verify getRentalsByUserIdAndIsActive() method throws Exception "
            + "for no rentals by userId")
    void getRentalsByUserIdAndIsActive_NoRentalsByUserId_Exception() {
        Long userId = 1L;
        boolean isActive = true;
        Pageable pageable = PageRequest.of(0, 10);

        when(rentalRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        Exception exception = assertThrows(DataNotFoundException.class,
                () -> rentalService.getRentalsByUserIdAndIsActive(userId, isActive, pageable));

        assertEquals("There's no rentals by user ID: " + userId, exception.getMessage());

        verify(rentalRepository, times(1)).findByUserId(userId);
        verifyNoMoreInteractions(rentalRepository);
    }

    @Test
    @DisplayName("Verify setRentalActualReturnDate() method sets actual return date as today,"
            + " increases car inventory by one and returns RentalDto if valid data")
    void setRentalActualReturnDate_ValidUserAndRentalId_ReturnsUpdatedRentalDto() {
        Long rentalId = 1L;
        Long userId = 1L;
        Long carId = 1L;

        User user = new User();
        user.setId(userId);

        Car car = new Car();
        car.setId(carId);
        car.setBrand("Chevrolet");
        car.setModel("Equinox");
        car.setType(Car.CarType.SUV);
        car.setInventory(2);

        Rental rental = Rental.builder()
                .id(rentalId)
                .rentalDate(LocalDate.now().minusDays(2))
                .returnDate(LocalDate.now().plusDays(1))
                .actualReturnDate(null)
                .user(user)
                .car(car)
                .isActive(true)
                .build();

        Rental updatedRental = Rental.builder()
                .id(rentalId)
                .rentalDate(rental.getRentalDate())
                .returnDate(rental.getReturnDate())
                .actualReturnDate(LocalDate.now())
                .user(user)
                .car(car)
                .isActive(false)
                .build();

        RentalDto expectedRentalDto = new RentalDto();
        expectedRentalDto.setId(rentalId);
        expectedRentalDto.setRentalDate(rental.getRentalDate());
        expectedRentalDto.setReturnDate(rental.getReturnDate());
        expectedRentalDto.setActualReturnDate(LocalDate.now());
        expectedRentalDto.setCarId(carId);
        expectedRentalDto.setUserId(userId);
        expectedRentalDto.setIsActive("INACTIVE");

        int expectedInventory = car.getInventory() + 1;

        when(rentalRepository.findByUserIdAndId(userId, rentalId)).thenReturn(Optional.of(rental));
        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(rentalRepository.save(any(Rental.class))).thenReturn(updatedRental);
        when(rentalMapper.toDto(updatedRental)).thenReturn(expectedRentalDto);

        RentalDto actualRentalDto = rentalService.setRentalActualReturnDate(user, rentalId);
        int actualInventory = car.getInventory();

        AssertionsForClassTypes.assertThat(actualRentalDto).isEqualTo(expectedRentalDto);
        assertEquals(expectedInventory, actualInventory);
        verify(rentalRepository, times(1)).findByUserIdAndId(userId, rentalId);
        verify(rentalRepository, times(1)).save(rental);
        verify(carRepository, times(1)).findById(carId);
        verify(carRepository, times(1)).save(car);
        verify(rentalMapper, times(1)).toDto(updatedRental);
        verifyNoMoreInteractions(rentalRepository, carRepository, rentalMapper);
    }

    @Test
    @DisplayName("Verify setRentalActualReturnDate() throws Exception for invalid rental id")
    void setRentalActualReturnDate_InvalidRentalId_ThrowsException() {
        Long rentalId = 1L;
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        when(rentalRepository.findByUserIdAndId(userId, rentalId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> rentalService.setRentalActualReturnDate(user, rentalId));

        assertEquals("Can't find a rental by id: " + rentalId, exception.getMessage());
        verify(rentalRepository, times(1)).findByUserIdAndId(userId, rentalId);
        verifyNoMoreInteractions(rentalRepository);
    }

    @Test
    @DisplayName("Verify setRentalActualReturnDate() throws Exception if rental is inactive")
    void setRentalActualReturnDate_InactiveRental_ThrowsException() {
        Long rentalId = 1L;
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        Rental rental = Rental.builder()
                .id(rentalId)
                .isActive(false)
                .user(user)
                .build();

        when(rentalRepository.findByUserIdAndId(userId, rentalId)).thenReturn(Optional.of(rental));

        Exception exception = assertThrows(IncorrectArgumentException.class,
                () -> rentalService.setRentalActualReturnDate(user, rentalId));

        assertEquals("Can't set new actual return date, rental id: " + rentalId
                + " is inactive. Try another rental", exception.getMessage());
        verify(rentalRepository, times(1)).findByUserIdAndId(userId, rentalId);
        verifyNoMoreInteractions(rentalRepository);
    }

    @Test
    @DisplayName("Verify setRentalActualReturnDate() throws Exception if rental date starts after "
            + "today")
    void setRentalActualReturnDate_FutureRentalDate_Exception() {
        Long rentalId = 1L;
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        Rental rental = Rental.builder()
                .id(rentalId)
                .rentalDate(LocalDate.now().plusDays(1))
                .isActive(true)
                .user(user)
                .build();

        when(rentalRepository.findByUserIdAndId(userId, rentalId)).thenReturn(Optional.of(rental));

        IncorrectArgumentException exception = assertThrows(IncorrectArgumentException.class,
                () -> rentalService.setRentalActualReturnDate(user, rentalId));

        assertEquals("Can't set actual return date as today because this rental starts later. "
                + "Rental id: " + rentalId, exception.getMessage());
        verify(rentalRepository, times(1)).findByUserIdAndId(userId, rentalId);
        verifyNoMoreInteractions(rentalRepository);
    }

    @Test
    @DisplayName("Verify calculateRentalTotalPrice() calculates correct total price for completed"
            + " rental without FINE status")
    void calculateRentalTotalPrice_RentalWithoutFine_ReturnsTotalPrice() {
        Long rentalId = 1L;
        BigDecimal dailyFee = BigDecimal.valueOf(100);
        Car car = new Car();
        car.setDailyFee(dailyFee);

        Rental rental = Rental.builder()
                .id(rentalId)
                .rentalDate(LocalDate.now().minusDays(5))
                .returnDate(LocalDate.now())
                .actualReturnDate(LocalDate.now().minusDays(2))
                .isActive(false)
                .car(car)
                .build();

        long expectedDuration = ChronoUnit.DAYS.between(
                rental.getRentalDate(), rental.getActualReturnDate());
        BigDecimal expectedTotalPrice = dailyFee.multiply(BigDecimal.valueOf(expectedDuration));

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));

        BigDecimal actualTotalPrice = rentalService.calculateRentalTotalPrice(rentalId, Payment.Type.PAYMENT);

        assertEquals(expectedTotalPrice, actualTotalPrice);
        verify(rentalRepository, times(1)).findById(rentalId);
        verifyNoMoreInteractions(rentalRepository);
    }

    @Test
    @DisplayName("Verify calculateRentalTotalPrice() calculates correct total price for completed"
            + " rental with FINE status")
    void calculateRentalTotalPrice_RentalWithFine_ReturnsTotalPriceWithFine() {
        Long rentalId = 1L;
        BigDecimal dailyFee = BigDecimal.valueOf(100.00);
        BigDecimal fineMultiplier = BigDecimal.valueOf(2.0);
        Car car = new Car();
        car.setDailyFee(dailyFee);

        Rental rental = Rental.builder()
                .id(rentalId)
                .rentalDate(LocalDate.now().minusDays(3))
                .returnDate(LocalDate.now())
                .actualReturnDate(LocalDate.now().plusDays(2))
                .isActive(false)
                .car(car)
                .build();

        long rentalDuration = ChronoUnit.DAYS.between(
                rental.getRentalDate(), rental.getReturnDate());
        BigDecimal totalPrice = dailyFee.multiply(BigDecimal.valueOf(rentalDuration));

        long overdueDuration = ChronoUnit.DAYS.between(
                rental.getReturnDate(), rental.getActualReturnDate());
        BigDecimal overdueMoneyToPay = dailyFee.multiply(
                BigDecimal.valueOf(overdueDuration)).multiply(fineMultiplier);
        BigDecimal expectedTotalPrice = totalPrice.add(overdueMoneyToPay);

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));

        BigDecimal actualTotalPrice = rentalService.calculateRentalTotalPrice(rentalId, Payment.Type.FINE);

        assertEquals(expectedTotalPrice, actualTotalPrice);
        verify(rentalRepository, times(1)).findById(rentalId);
        verifyNoMoreInteractions(rentalRepository);
    }

    @Test
    @DisplayName("Verify calculateRentalTotalPrice() throws Exception for active rental")
    void calculateRentalTotalPrice_ActiveRental_ThrowsIncorrectArgumentException() {
        Long rentalId = 1L;
        Rental rental = Rental.builder()
                .id(rentalId)
                .isActive(true)
                .build();

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));

        Exception exception = assertThrows(IncorrectArgumentException.class,
                () -> rentalService.calculateRentalTotalPrice(rentalId, Payment.Type.PAYMENT));

        assertEquals("Can't process, rental is still active. You need to complete "
                + "this rental first, ID: " + rentalId, exception.getMessage());
        verify(rentalRepository, times(1)).findById(rentalId);
        verifyNoMoreInteractions(rentalRepository);
    }

    @Test
    @DisplayName("Verify calculateRentalTotalPrice() throws Exception for wrong rental Id")
    void calculateRentalTotalPrice_InvalidRentalId_ThrowsException() {
        Long rentalId = 100L;

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> rentalService.calculateRentalTotalPrice(rentalId, Payment.Type.PAYMENT));

        assertEquals("Can't find a rental by id: " + rentalId, exception.getMessage());
        verify(rentalRepository, times(1)).findById(rentalId);
        verifyNoMoreInteractions(rentalRepository);
    }

    @Test
    @DisplayName("Verify notifications are sent for overdue rentals")
    void getAllOverdueRentalsAndSendNotificationToUser_WithOverdueRentals_SendsNotifications() {
        User user = new User();
        user.setFirstName("Mr.");
        user.setLastName("First");

        Car car = new Car();
        car.setBrand("Chevrolet");
        car.setModel("Equinox");
        car.setType(Car.CarType.SUV);

        Rental overdueRental = Rental.builder()
                .id(1L)
                .user(user)
                .car(car)
                .returnDate(LocalDate.now().minusDays(5))
                .isActive(true)
                .build();
        Set<Rental> rentals = Set.of(overdueRental);
        user.setRentals(rentals);

        when(rentalRepository.findByIsActive(true)).thenReturn(List.of(overdueRental));

        rentalService.getAllOverdueRentalsAndSendNotificationToUser();

        long expectedOverduePeriod = 5L;
        long actualOverduePeriod = ChronoUnit.DAYS.between(
                overdueRental.getReturnDate(), LocalDate.now());

        String actualMessage = String.format(
                """
                        Overdue Rental ID %s: User %s %s has an overdue rental for %s %s %s.
                        Rental Date: %s, Expected Return Date: %s
                        The overdue period is %d days
                        """,
                overdueRental.getId(), user.getFirstName(), user.getLastName(),
                car.getBrand(), car.getModel(), car.getType(),
                overdueRental.getRentalDate(), overdueRental.getReturnDate(),
                actualOverduePeriod, "days"
        );

        assertEquals(expectedOverduePeriod, actualOverduePeriod);
        verify(rentalRepository, times(1)).findByIsActive(true);
        verify(telegramNotificationService, times(1)).sendMessage(actualMessage);
        verifyNoMoreInteractions(telegramNotificationService, rentalRepository);
    }

    @Test
    @DisplayName("Verify notification was sent when no rentals are overdue")
    void getAllOverdueRentalsAndSendNotificationToUser_NoOverdueRentals_SendsNoOverdueMessage() {
        when(rentalRepository.findByIsActive(true)).thenReturn(List.of());

        rentalService.getAllOverdueRentalsAndSendNotificationToUser();

        verify(telegramNotificationService, times(1)).sendMessage("No rentals overdue today!");
        verify(rentalRepository, times(1)).findByIsActive(true);
        verifyNoMoreInteractions(telegramNotificationService, rentalRepository);
    }
}
