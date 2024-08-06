package com.example.project.dto.rental;

import java.time.LocalDate;

public record CreateRentalReturnRequestDto(LocalDate actualReturnDate) {
}
