package com.example.project.repository;

import com.example.project.model.Rental;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    List<Rental> findByUserId(Long userId);

    Optional<Rental> findByUserIdAndId(Long userId, Long rentalId);

    List<Rental> findByUserIdAndIsActive(Long userId, boolean isActive);

    List<Rental> findByIsActive(boolean isActive);

}
