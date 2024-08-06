package com.example.project.service;

import com.example.project.dto.rental.CreateRentalRequestDto;
import com.example.project.dto.rental.RentalDto;
import com.example.project.model.Payment;
import com.example.project.model.User;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface RentalService {
    RentalDto save(User user, CreateRentalRequestDto requestDto);

    RentalDto getById(Long userId, Long id);

    List<RentalDto> getRentalsByUserIdAndIsActive(Long userId, boolean isActive, Pageable pageable);

    RentalDto setRentalActualReturnDate(User user, Long rentalId);

    BigDecimal calculateRentalTotalPrice(Long rentalId, Payment.Type type);

    void getAllOverdueRentalsAndSendNotificationToUser();
}
