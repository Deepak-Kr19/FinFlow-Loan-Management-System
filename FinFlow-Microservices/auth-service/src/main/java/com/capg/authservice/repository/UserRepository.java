package com.capg.authservice.repository;

import com.capg.authservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Spring Data JPA repository for User entity.
 * Provides CRUD operations and custom query methods for the users table.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their email address.
     * Used during login to verify credentials and during registration to check duplicates.
     *
     * @param email the user's email address
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);
}
