package com.example.project.mapper;

import com.example.project.config.MapperConfig;
import com.example.project.dto.payment.PaymentDto;
import com.example.project.model.Payment;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface PaymentMapper {
    PaymentDto toDto(Payment payment);
}

