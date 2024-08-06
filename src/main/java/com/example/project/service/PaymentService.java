package com.example.project.service;

import com.example.project.dto.payment.PaymentDto;
import com.example.project.dto.payment.PaymentRequestDto;
import com.example.project.dto.payment.PaymentResponseDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    List<PaymentDto> findAllByUserId(Long userId, Pageable pageable);

    PaymentResponseDto createPaymentSession(PaymentRequestDto paymentRequestDto, Long userId);

    void handleSuccessfulPayment(String sessionId);

    void handleCancelledPayment(String sessionId);
}
