package com.example.project.dto.user;

import com.example.project.model.Role;
import java.util.List;
import java.util.Set;
import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Set<Role.RoleName> roleNames;
    private List<Long> rentalIds;
}
