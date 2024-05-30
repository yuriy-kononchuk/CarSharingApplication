package com.example.project.controller;

import com.example.project.dto.payment.PaymentDto;
import com.example.project.dto.payment.PaymentRequestDto;
import com.example.project.dto.payment.PaymentResponseDto;
import com.example.project.exception.AccessDeniedException;
import com.example.project.model.User;
import com.example.project.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payment management", description = "Endpoints for mapping payments")
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/payments")
public class PaymentController {
    private final PaymentService paymentService;

    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('CUSTOMER')")
    @GetMapping
    @Operation(summary = "Get all payments",
            description = "Get a list of payments based on user role")
    @ApiResponse(responseCode = "200", description = "Got all user's payments by id")
    public List<PaymentDto> getAllPayments(
            @RequestParam Long userId,
            Authentication authentication,
            Pageable pageable
    ) {
        User user = (User) authentication.getPrincipal();
        Set<String> authorities = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        boolean isManager = authorities.contains("MANAGER");
        boolean isCustomer = authorities.contains("CUSTOMER");
        if (isManager) {
            return paymentService.findAllByUserId(userId, pageable);
        } else if (isCustomer) {
            return paymentService.findAllByUserId(user.getId(), pageable);
        } else {
            throw new AccessDeniedException("You don't have sufficient rights"
                    + " to this resource: ");
        }
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @Operation(summary = "Create a payment session", description = "Create a payment session")
    @ApiResponse(responseCode = "201", description = "Payment session is created")
    public PaymentResponseDto createPaymentSession(@RequestBody PaymentRequestDto requestDto,
                                                   Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return paymentService.createPaymentSession(requestDto, user.getId());
    }

    @GetMapping("/success")
    @Operation(summary = "Handle successful payment",
            description = "Handles successful payment completion")
    public ResponseEntity<String> handleSuccessfulPayment(@RequestParam String sessionId) {
        paymentService.handleSuccessfulPayment(sessionId);
        return ResponseEntity.ok("Payment is successful! Session ID: " + sessionId);
    }

    @GetMapping("/cancel")
    @Operation(summary = "Handle cancelled payment", description = "Handles payment cancellation")
    public ResponseEntity<String> handleCancelledPayment(@RequestParam String sessionId) {
        paymentService.handleCancelledPayment(sessionId);
        return ResponseEntity.ok("Payment cancelled. Session ID: " + sessionId
                + System.lineSeparator()
                + ". Payment can be made later, but the session is available for only 24 hours");
    }
}
