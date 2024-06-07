package com.example.project.mapper;

import com.example.project.dto.payment.PaymentDto;
import com.example.project.model.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapperImpl implements PaymentMapper {
    @Override
    public PaymentDto toDto(Payment payment) {
        if (payment == null) {
            return null;
        }
        PaymentDto dto = new PaymentDto();
        dto.setId(payment.getId());
        dto.setRentalId(payment.getRentalId());
        dto.setPayAmount(payment.getPayAmount());
        dto.setStatus(payment.getStatus());
        dto.setType(payment.getType());
        dto.setSessionId(payment.getSessionId());
        dto.setSessionUrl(payment.getSessionUrl());
        return dto;
    }
}
