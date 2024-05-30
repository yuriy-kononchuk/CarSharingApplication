package com.example.project.mapper;

import com.example.project.dto.rental.RentalDto;
import com.example.project.model.Rental;
import org.springframework.stereotype.Component;

@Component
public class RentalMapperImpl implements RentalMapper {
    @Override
    public RentalDto toDto(Rental rental) {
        if (rental == null) {
            return null;
        }
        RentalDto dto = new RentalDto();
        dto.setId(rental.getId());
        dto.setRentalDate(rental.getRentalDate());
        dto.setReturnDate(rental.getReturnDate());
        dto.setActualReturnDate(rental.getActualReturnDate());
        dto.setUserId(rental.getUser().getId());
        dto.setCarId(rental.getCar().getId());
        dto.setIsActive(String.valueOf(rental.isActive()));
        return dto;
    }
}
