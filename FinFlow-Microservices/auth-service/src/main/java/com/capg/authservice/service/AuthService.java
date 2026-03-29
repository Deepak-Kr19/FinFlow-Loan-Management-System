package com.capg.authservice.service;

import com.capg.authservice.dto.AuthRequest;
import com.capg.authservice.dto.AuthResponse;
import com.capg.authservice.dto.RegisterRequest;
import com.capg.authservice.entity.User;
import com.capg.authservice.repository.UserRepository;
import com.capg.authservice.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer for authentication and user management.
 * Handles user registration, login with JWT token generation,
 * and admin operations (list/update users).
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Registers a new user in the system.
     * Encodes the password using BCrypt before saving.
     * Defaults role to ROLE_APPLICANT if not specified.
     *
     * @param request registration details (name, email, password, role)
     * @return success message
     * @throws RuntimeException if the email is already registered
     */
    public String register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Check for duplicate email
        if(userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Registration failed — email already exists: {}", request.getEmail());
            throw new RuntimeException("Email already exists");
        }

        // Build and save the new user entity
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Hash password with BCrypt
        user.setRole(request.getRole() != null ? request.getRole() : "ROLE_APPLICANT");
        userRepository.save(user);

        log.info("User registered successfully: {} (role: {})", request.getEmail(), user.getRole());
        return "User registered successfully";
    }

    /**
     * Authenticates a user and returns a JWT token on success.
     * The token contains the user's email, role, and ID as claims.
     *
     * @param request login credentials (email, password)
     * @return AuthResponse containing the JWT token and user role
     * @throws RuntimeException if user not found or password doesn't match
     */
    public AuthResponse login(AuthRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        // Look up user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed — user not found: {}", request.getEmail());
                    return new RuntimeException("User not found");
                });

        // Verify password against stored BCrypt hash
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed — invalid password for: {}", request.getEmail());
            throw new RuntimeException("Invalid credentials");
        }

        // Generate JWT token with user details
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId());
        log.info("Login successful for: {} (role: {}, userId: {})", user.getEmail(), user.getRole(), user.getId());
        return new AuthResponse(token, user.getRole());
    }

    /**
     * Fetches all registered users (admin operation).
     * Called by the Admin Service via REST to display user list.
     *
     * @return list of all users
     */
    public List<User> getAllUsers() {
        log.info("Fetching all users");
        List<User> users = userRepository.findAll();
        log.info("Found {} users", users.size());
        return users;
    }

    /**
     * Updates an existing user's details (admin operation).
     * Only updates fields that are non-null in the request.
     *
     * @param id      the user ID to update
     * @param request fields to update (name, email, role)
     * @return success message
     * @throws RuntimeException if user not found
     */
    public String updateUser(Long id, RegisterRequest request) {
        log.info("Updating user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Update failed — user not found with id: {}", id);
                    return new RuntimeException("User not found");
                });

        // Only update non-null fields (partial update)
        if (request.getName() != null) user.setName(request.getName());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getRole() != null) user.setRole(request.getRole());
        userRepository.save(user);

        log.info("User updated successfully: id={}", id);
        return "User updated successfully";
    }
}
