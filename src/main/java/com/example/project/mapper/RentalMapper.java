package com.example.project.mapper;

import com.example.project.dto.rental.RentalDto;
import com.example.project.model.Rental;
import org.mapstruct.Mapper;
import org.mapstruct.MapperConfig;

@Mapper(config = MapperConfig.class)
public interface RentalMapper {
    RentalDto toDto(Rental rental);
}
