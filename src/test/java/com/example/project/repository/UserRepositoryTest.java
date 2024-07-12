package com.example.project.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.project.model.User;
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
        "classpath:database/users/delete-test-users-from-users-table.sql",
        "classpath:database/rentals/add-test-rentals-to-rentals-table.sql",
        "classpath:database/users/add-test-roles-to-roles-users-roles-tables.sql"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/users/delete-test-users-from-users-table.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Verify to find user by email if present correctly")
    void findByEmail_ValidEmail_ReturnsUserOptional() {
        String email = "testuser1@example.com";

        Optional<User> actualUser = userRepository.findByEmail(email);

        Assertions.assertThat(actualUser).isPresent();
        assertEquals(1L, actualUser.get().getId());
        assertEquals("testuser1@example.com", actualUser.get().getEmail());
        assertEquals("user", actualUser.get().getFirstName());
        assertEquals("first", actualUser.get().getLastName());
        Assertions.assertThat(actualUser.get().getRoles()).isNotEmpty();
    }

    @Test
    @DisplayName("Verify no user found with wrong email")
    void findByEmail_InvalidEmail_ReturnsEmptyOptional() {
        String email = "wrong_email@example.com";

        Optional<User> foundUser = userRepository.findByEmail(email);

        Assertions.assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Verify to find user by id with roles and rentals")
    void findById_ValidId_ReturnsUserOptional() {
        Long userId = 1L;
        List<User> allUsers = userRepository.findAll();
        System.out.println("All users in the database: " + allUsers);

        Optional<User> actualUser = userRepository.findById(userId);

        Assertions.assertThat(actualUser).isPresent();
        assertEquals(1L, actualUser.get().getId());
        assertEquals("testuser1@example.com", actualUser.get().getEmail());
        assertEquals("user", actualUser.get().getFirstName());
        assertEquals("first", actualUser.get().getLastName());
        Assertions.assertThat(actualUser.get().getRoles()).isNotEmpty();
        Assertions.assertThat(actualUser.get().getRentals()).isNotEmpty();
    }

    @Test
    @DisplayName("Verify no user found with wrong id")
    void findById_InvalidId_ReturnsEmptyOptional() {
        Long userId = 100L;

        Optional<User> foundUser = userRepository.findById(userId);

        Assertions.assertThat(foundUser).isEmpty();
    }
}
