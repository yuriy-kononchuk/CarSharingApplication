package com.example.project.dto.rental;

import java.time.LocalDate;
import lombok.Data;

@Data
public class RentalDto {
    private Long id;
    private LocalDate rentalDate;
    private LocalDate returnDate;
    private LocalDate actualReturnDate;
    private Long carId;
    private Long userId;
    private String isActive;
}
