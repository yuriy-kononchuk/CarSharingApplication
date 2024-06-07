package com.example.project.controller;

import com.example.project.dto.rental.CreateRentalRequestDto;
import com.example.project.dto.rental.RentalDto;
import com.example.project.exception.AccessDeniedException;
import com.example.project.model.User;
import com.example.project.service.RentalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Rental management", description = "Endpoints for mapping rentals")
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/rentals")
public class RentalController {
    private final RentalService rentalService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @Operation(summary = "Create a new rental", description = "Create a new rental")
    @ApiResponse(responseCode = "201", description = "New rental is successfully created")
    public RentalDto createRental(@RequestBody @Valid CreateRentalRequestDto requestDto,
                                  Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return rentalService.save(user, requestDto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user's rental by id",
            description = "Get a user's rental info by id")
    @ApiResponse(responseCode = "200", description = "Got a user's rental by id")
    public RentalDto getById(Authentication authentication, @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        return rentalService.getById(user.getId(), id);
    }

    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('CUSTOMER')")
    @GetMapping
    @Operation(summary = "Get all user's active rentals",
            description = "Get a list of rentals with 'is active' condition based on user role")
    @ApiResponse(responseCode = "200",
            description = "Got a rentals list by id with 'isActive condition")
    public List<RentalDto> getAllRentalsByUserAndIsActive(
            Authentication authentication,
            @RequestParam Long userId,
            @RequestParam boolean isActive,
            Pageable pageable) {
        User user = (User) authentication.getPrincipal();
        Set<String> authorities = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        boolean isManager = authorities.contains("MANAGER");
        boolean isCustomer = authorities.contains("CUSTOMER");
        if (isCustomer) {
            return rentalService.getRentalsByUserIdAndIsActive(user.getId(), isActive, pageable);
        } else if (isManager) {
            return rentalService.getRentalsByUserIdAndIsActive(userId, isActive, pageable);
        } else {
            throw new AccessDeniedException("You don't have sufficient rights"
                    + " to access this resource: ");
        }
    }

    @PostMapping("/{id}/return")
    @Operation(summary = "Set a rental's actual return date",
            description = "Set a rental's actual return date")
    @ApiResponse(responseCode = "200", description = "Actual return date is successfully added")
    public RentalDto setRentalActualReturnDate(Authentication authentication,
                                               @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        return rentalService.setRentalActualReturnDate(user, id);
    }

}
