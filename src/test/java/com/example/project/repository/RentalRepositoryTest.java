package com.example.project.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.project.model.Rental;
import java.util.List;
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
@Sql(scripts = {
        "classpath:database/rentals/delete-test-rentals-from-rentals-table.sql",
        "classpath:database/rentals/add-test-rentals-to-rentals-table.sql"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/rentals/delete-test-rentals-from-rentals-table.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class RentalRepositoryTest {
    @Autowired
    private RentalRepository rentalRepository;

    @Test
    @DisplayName("Find rentals by user ID")
    void findByUserId_ValidUserId_ReturnsRentalList() {
        Long userOneId = 1L;
        Long userTwoId = 2L;

        List<Rental> rentalsUser1 = rentalRepository.findByUserId(userOneId);
        List<Rental> rentalsUser2 = rentalRepository.findByUserId(userTwoId);

        Assertions.assertThat(rentalsUser1).isNotEmpty();
        Assertions.assertThat(rentalsUser2).isNotEmpty();
        assertEquals(2, rentalsUser1.size());
        assertEquals(2, rentalsUser2.size());
        Assertions.assertThat(rentalsUser1.get(0).getUser().getId()).isEqualTo(userOneId);
        Assertions.assertThat(rentalsUser2.get(0).getUser().getId()).isEqualTo(userTwoId);
    }

    @Test
    @DisplayName("Verify no rentals found with wrong user ID")
    void findByUserId_InvalidUserId_ReturnsEmptyList() {
        Long userId = 100L;
        List<Rental> rentals = rentalRepository.findByUserId(userId);

        assertEquals(0, rentals.size());
    }

    @Test
    @DisplayName("Find rental by user ID and rental ID")
    void findByUserIdAndId_ValidUserIdAndRentalId_ReturnsRentalOptional() {
        Long userId = 1L;
        Long rentalId = 1L;

        Optional<Rental> rental = rentalRepository.findByUserIdAndId(userId, rentalId);

        Assertions.assertThat(rental).isPresent();
        assertEquals(rentalId, rental.get().getId());
        assertEquals(userId, rental.get().getUser().getId());
    }

    @Test
    @DisplayName("Verify no rental found with wrong user and rental IDs")
    void findByUserIdAndId_InvalidUserIdAndRentalId_ReturnsEmptyOptional() {
        Long userId = 10L;
        Long rentalId = 10L;

        Optional<Rental> rental = rentalRepository.findByUserIdAndId(userId, rentalId);

        Assertions.assertThat(rental).isEmpty();
    }

    @Test
    @DisplayName("Find rentals by user ID with active status")
    void findByUserIdAndIsActive_ValidUserIdAndIsActive_ReturnsRentalList() {
        Long userOneId = 1L;
        Long userTwoId = 2L;
        boolean isActive = true;

        List<Rental> rentalsUser1 = rentalRepository.findByUserIdAndIsActive(userOneId, isActive);
        List<Rental> rentalsUser2 = rentalRepository.findByUserIdAndIsActive(userTwoId, isActive);

        Assertions.assertThat(rentalsUser1).isNotEmpty();
        Assertions.assertThat(rentalsUser2).isNotEmpty();
        assertEquals(2, rentalsUser1.size());
        assertEquals(1, rentalsUser2.size());
        Assertions.assertThat(rentalsUser1.get(0).getUser().getId()).isEqualTo(userOneId);
        Assertions.assertThat(rentalsUser2.get(0).getUser().getId()).isEqualTo(userTwoId);
    }

    @Test
    @DisplayName("Verify no rentals found with valid user ID and no active rental ")
    void findByUserIdAndIsActive_ValidUserIdAndNoActiveRental_ReturnsEmptyList() {
        Long userId = 3L;
        boolean isActive = true;
        List<Rental> rentals = rentalRepository.findByUserIdAndIsActive(userId, isActive);

        assertEquals(0, rentals.size());
    }

    @Test
    @DisplayName("Find rentals with active status")
    void findByIsActive_ValidIsActive_ReturnsRentalList() {
        boolean isActive = true;

        List<Rental> rentals = rentalRepository.findByIsActive(isActive);

        assertEquals(3, rentals.size());
        assertEquals(1L, rentals.get(0).getId());
        assertEquals(2L, rentals.get(1).getId());
        assertEquals(4L, rentals.get(2).getId());
        Assertions.assertThat(rentals).allMatch(Rental::isActive);
    }

    @Test
    @DisplayName("Find rentals with inactive status")
    void findByIsActive_ValidIsNotActive_ReturnsRentalList() {
        boolean isActive = false;

        List<Rental> rentals = rentalRepository.findByIsActive(isActive);

        assertEquals(2, rentals.size());
        assertEquals(3L, rentals.get(0).getId());
        assertEquals(5L, rentals.get(1).getId());
        Assertions.assertThat(rentals).allMatch(rental -> !rental.isActive());
    }
}
