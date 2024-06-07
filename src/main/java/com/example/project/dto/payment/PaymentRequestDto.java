package com.example.project.dto.payment;

import com.example.project.model.Payment;

public record PaymentRequestDto(Long rentalId, Payment.Type type) {
}
