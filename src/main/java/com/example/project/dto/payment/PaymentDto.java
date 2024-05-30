package com.example.project.dto.payment;

import com.example.project.model.Payment;
import java.math.BigDecimal;
import java.net.URL;
import lombok.Data;

@Data
public class PaymentDto {
    private Long id;
    private Long rentalId;
    private BigDecimal payAmount;
    private Payment.Status status;
    private Payment.Type type;
    private String sessionId;
    private URL sessionURL;
}
