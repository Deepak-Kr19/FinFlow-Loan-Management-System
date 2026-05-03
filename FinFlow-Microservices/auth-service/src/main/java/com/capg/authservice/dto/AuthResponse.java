package com.capg.authservice.dto;

/**
 * DTO returned after a successful login.
 * Contains the JWT token, user identity, and role for UI-side authorization.
 */
public class AuthResponse {
    /** JWT token to be sent in the Authorization header as "Bearer {token}" */
    private String token;

    /** User's role (e.g., ROLE_APPLICANT, ROLE_ADMIN) for frontend routing */
    private String role;

    /** User's database ID for frontend state management */
    private Long userId;

    /** User's full name for display in the UI header/profile */
    private String name;

    /** User's email for display in the profile section */
    private String email;

    public AuthResponse() {}

    public AuthResponse(String token, String role, Long userId, String name, String email) {
        this.token = token;
        this.role = role;
        this.userId = userId;
        this.name = name;
        this.email = email;
    }

    // --- Getters and Setters ---
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
