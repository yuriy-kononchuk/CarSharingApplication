package com.example.project.repository;

import com.example.project.model.Payment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findBySessionId(String sessionId);

    Optional<Payment> findByRentalId(Long rentalId);
}
