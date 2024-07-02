package com.example.project.service;

import com.example.project.dto.payment.PaymentDto;
import com.example.project.dto.rental.RentalDto;
import com.example.project.exception.AccessDeniedException;
import com.example.project.model.Role;
import com.example.project.model.User;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class RoleBasedAccessService {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RentalService rentalService;

    public List<PaymentDto> getPaymentsBasedOnRole(Long userId, User loggedInUser,
                                                   Pageable pageable, Set<String> authorities) {
        Long userIdBasedOnRole = getUserIdBasedOnRole(userId, loggedInUser, authorities);
        return paymentService.findAllByUserId(userIdBasedOnRole, pageable);
    }

    public List<RentalDto> getRentalsBasedOnRole(Long userId, boolean isActive, User loggedInUser,
                                                 Pageable pageable, Set<String> authorities) {
        Long userIdBasedOnRole = getUserIdBasedOnRole(userId, loggedInUser, authorities);
        return rentalService.getRentalsByUserIdAndIsActive(userIdBasedOnRole, isActive, pageable);
    }

    private Long getUserIdBasedOnRole(Long userId, User loggedInUser, Set<String> authorities) {
        if (authorities.contains(Role.RoleName.MANAGER.name())) {
            return userId;
        } else if (authorities.contains(Role.RoleName.CUSTOMER.name())) {
            return loggedInUser.getId();
        } else {
            throw new AccessDeniedException("You don't have sufficient rights to this resource.");
        }
    }
}
