package com.example.project.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Data
@SQLDelete(sql = "UPDATE rentals SET is_deleted = TRUE WHERE id = ?")
@Where(clause = "is_deleted = FALSE")
@Table(name = "rentals")
@NoArgsConstructor
public class Rental {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Column(name = "rental_date", nullable = false)
    private LocalDate rentalDate;
    @NotNull
    @Column(name = "return_date", nullable = false)
    private LocalDate returnDate;
    @NotNull
    @Column(name = "return_date_actual", nullable = false)
    private LocalDate actualReturnDate;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "car_id")
    private Car car;
    @NotNull
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false)
    private boolean isActive;
    @Column(nullable = false)
    private boolean isDeleted = false;

    public Rental(Long id) {
        this.id = id;
    }
}
