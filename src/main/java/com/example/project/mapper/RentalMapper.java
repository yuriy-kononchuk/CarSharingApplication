package com.example.project.mapper;

import com.example.project.config.MapperConfig;
import com.example.project.dto.rental.RentalDto;
import com.example.project.model.Rental;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface RentalMapper {
    @Mapping(source = "car.id", target = "carId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "active", target = "isActive")
    RentalDto toDto(Rental rental);
}
