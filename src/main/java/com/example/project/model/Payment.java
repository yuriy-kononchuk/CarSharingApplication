package com.example.project.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.net.URL;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "payments")
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Column(name = "payment_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;
    @Column(name = "payment_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private Type type;
    @NotNull
    @Column(name = "rental_id", nullable = false)
    private Long rentalId;
    @NotNull
    @Column(name = "session_url", length = 512, nullable = false)
    private URL sessionURL;
    @NotBlank
    @Column(name = "session_id", nullable = false) // OR @JoinColumn?
    private String sessionId;
    @NotNull
    @Min(value = 0)
    @Column(name = "amount", nullable = false)
    private BigDecimal payAmount;

    public Payment(Long id) {
        this.id = id;
    }

    public enum Status {
        PENDING,
        PAID,
        EXPIRED,
        CANCELLED
    }

    public enum Type {
        PAYMENT,
        FINE
    }
}
