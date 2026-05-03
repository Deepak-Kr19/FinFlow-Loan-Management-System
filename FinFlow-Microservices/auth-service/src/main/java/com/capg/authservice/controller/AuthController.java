package com.capg.authservice.controller;

import com.capg.authservice.dto.AuthRequest;
import com.capg.authservice.dto.AuthResponse;
import com.capg.authservice.dto.RegisterRequest;
import com.capg.authservice.entity.User;
import com.capg.authservice.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;



/**
 * ****************************************
 * AUTHOR: DEEPAK KUMAR
 * CLASS: AuthController
 * 
 * ****************************************
 * 
 * REST controller for authentication endpoints.
 * Base path: /auth
 *
 * Endpoints:
 * - POST /auth/signup       → Register a new user
 * - POST /auth/login        → Login and receive JWT token
 * - GET  /auth/admin/users  → List all users (called by Admin Service)
 * - PUT  /auth/admin/users/{id} → Update a user (called by Admin Service)
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** Register a new user with validated input */
    @PostMapping("/signup")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /auth/signup — email: {}", request.getEmail());
        return ResponseEntity.ok(authService.register(request));
    }

    /** Authenticate user and return JWT token + role */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        log.info("POST /auth/login — email: {}", request.getEmail());
        return ResponseEntity.ok(authService.login(request));
    }

    /** Admin endpoint: Fetch all registered users */
    @GetMapping("/admin/users")
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("GET /auth/admin/users");
        return ResponseEntity.ok(authService.getAllUsers());
    }

    /** Admin endpoint: Update user details */
    @PutMapping("/admin/users/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Long id, @RequestBody RegisterRequest request) {
        log.info("PUT /auth/admin/users/{}", id);
        return ResponseEntity.ok(authService.updateUser(id, request));
    }

    /** Fetch the current user's profile using X-User-Id header */
    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(@RequestHeader("X-User-Id") Long userId) {
        log.info("GET /auth/profile — userId: {}", userId);
        return ResponseEntity.ok(authService.getProfile(userId));
    }
}
