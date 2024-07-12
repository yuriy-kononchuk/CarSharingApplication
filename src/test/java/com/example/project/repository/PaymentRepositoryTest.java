package com.example.project.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.project.model.Payment;
import java.math.BigDecimal;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "classpath:database/payments/add-test-payments-to-payments-table.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/payments/delete-test-payments-from-payments-table.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class PaymentRepositoryTest {
    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("Verify to find payment by session ID")
    void findBySessionId_ValidSessionId_ReturnsPaymentOptional() {
        String sessionId = "session123";

        Optional<Payment> actualPayment = paymentRepository.findBySessionId(sessionId);

        Assertions.assertThat(actualPayment).isPresent();
        assertEquals(1L, actualPayment.get().getId());
        assertEquals(sessionId, actualPayment.get().getSessionId());
        assertEquals(Payment.Status.PAID, actualPayment.get().getStatus());
        assertEquals(Payment.Type.PAYMENT, actualPayment.get().getType());
        assertEquals(0, actualPayment.get().getPayAmount().compareTo(BigDecimal.valueOf(100.00)));
    }

    @Test
    @DisplayName("Verify no payment found with wrong session ID")
    void findBySessionId_InvalidSessionId_ReturnsEmptyOptional() {
        String sessionId = "invalid-session-id";

        Optional<Payment> actualPayment = paymentRepository.findBySessionId(sessionId);

        Assertions.assertThat(actualPayment).isEmpty();
    }

    @Test
    @DisplayName("Verify to find payment by rental ID")
    void findByRentalId_ValidRentalId_ReturnsPaymentOptional() {
        Long rentalId = 1L;

        Optional<Payment> actualPayment = paymentRepository.findByRentalId(rentalId);

        Assertions.assertThat(actualPayment).isPresent();
        assertEquals(1L, actualPayment.get().getId());
        assertEquals(rentalId, actualPayment.get().getRentalId());
        assertEquals(Payment.Status.PAID, actualPayment.get().getStatus());
        assertEquals(Payment.Type.PAYMENT, actualPayment.get().getType());
        assertEquals(0, actualPayment.get().getPayAmount().compareTo(BigDecimal.valueOf(100.00)));
    }

    @Test
    @DisplayName("Verify no payment found with wrong rental ID")
    void findByRentalId_InvalidRentalId_ReturnsEmptyOptional() {
        Long rentalId = 100L;

        Optional<Payment> foundPayment = paymentRepository.findByRentalId(rentalId);

        Assertions.assertThat(foundPayment).isEmpty();
    }
}
